package com.bitwiserain.pbbg.app.db.usecase

import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.DexTable
import com.bitwiserain.pbbg.app.db.repository.InventoryTable
import com.bitwiserain.pbbg.app.db.repository.ItemHistoryTable
import com.bitwiserain.pbbg.app.db.repository.MaterializedItemTable
import com.bitwiserain.pbbg.app.db.repository.UserStatsTable
import com.bitwiserain.pbbg.app.db.repository.farm.MaterializedPlantTable
import com.bitwiserain.pbbg.app.db.repository.farm.MaterializedPlantTable.PlantForm
import com.bitwiserain.pbbg.app.db.repository.farm.PlotListTable
import com.bitwiserain.pbbg.app.db.repository.farm.PlotTable
import com.bitwiserain.pbbg.app.domain.model.BaseItem
import com.bitwiserain.pbbg.app.domain.model.MaterializedItem
import com.bitwiserain.pbbg.app.domain.model.farm.IBasePlant
import com.bitwiserain.pbbg.app.domain.model.farm.IMaterializedPlant
import com.bitwiserain.pbbg.app.domain.model.farm.MaterializedPlant
import com.bitwiserain.pbbg.app.domain.model.farm.Plot
import com.bitwiserain.pbbg.app.domain.model.itemdetails.ItemHistoryInfo
import com.bitwiserain.pbbg.app.domain.usecase.EmptyPlotException
import com.bitwiserain.pbbg.app.domain.usecase.FarmUC
import com.bitwiserain.pbbg.app.domain.usecase.InsufficientItemQuantity
import com.bitwiserain.pbbg.app.domain.usecase.InventoryItemNotFoundException
import com.bitwiserain.pbbg.app.domain.usecase.ItemNotPlantableException
import com.bitwiserain.pbbg.app.domain.usecase.OccupiedPlotException
import com.bitwiserain.pbbg.app.domain.usecase.PlantNotHarvestableException
import com.bitwiserain.pbbg.app.domain.usecase.UserPlotNotFoundException
import java.time.Clock

class FarmUCImpl(
    private val transaction: Transaction,
    private val clock: Clock,
    private val dexTable: DexTable,
    private val inventoryTable: InventoryTable,
    private val itemHistoryTable: ItemHistoryTable,
    private val materializedItemTable: MaterializedItemTable,
    private val materializedPlantTable: MaterializedPlantTable,
    private val plotTable: PlotTable,
    private val plotListTable: PlotListTable,
    private val userStatsTable: UserStatsTable,
) : FarmUC {

    override fun getPlots(userId: Int): List<Plot> = transaction {
        getPlotsInOrder(userId)
    }

    private fun getPlotsInOrder(userId: Int): List<Plot> {
        val plotIdList = plotListTable.get(userId)

        return plotTable.getPlots(userId).sortedBy { plotIdList.indexOf(it.id) }
    }

    override fun plant(userId: Int, plotId: Long, itemId: Long): Plot = transaction {
        val now = clock.instant()

        /* Make sure user owns this plot */
        val plot = plotTable.getPlot(userId, plotId) ?: throw UserPlotNotFoundException()

        /* Make sure plot is empty */
        if (plot.plant != null) throw OccupiedPlotException()

        /* Make sure user owns this item */
        val item = (inventoryTable.getInventoryItem(userId, itemId) ?: throw InventoryItemNotFoundException(itemId)).item
        /* and that there is at least 1 item */
        if (item is MaterializedItem.Stackable && item.quantity < 1) throw InsufficientItemQuantity()

        val baseItem = item.base

        /* Make sure item can be planted */
        if (baseItem !is BaseItem.Plantable) throw ItemNotPlantableException()

        /* Create plant in database */
        val plantId = materializedPlantTable.insertPlantAndGetId(PlantForm(
            enum = baseItem.basePlant.enum,
            cycleStart = now,
            isMaturable = baseItem.basePlant is IBasePlant.Maturable
        ))

        /* Update plot that was planted into */
        plotTable.updatePlot(userId, plot.id, plantId)

        /* Remove plantable item that was used */
        if (item is MaterializedItem.Stackable) {
            materializedItemTable.updateQuantity(itemId, -1)
        } else {
            inventoryTable.removeItem(userId, itemId)
        }

        return@transaction plotTable.getPlot(userId, plot.id)!!
    }

    override fun harvest(userId: Int, plotId: Long): Plot = transaction {
        val now = clock.instant()

        /* Make sure user owns this plot */
        val plot = plotTable.getPlot(userId, plotId) ?: throw UserPlotNotFoundException()

        /* Make sure plot is not empty */
        val (plantId, plant) = plot.plant ?: throw EmptyPlotException()

        /* Make sure plant can be harvested */
        if (!plant.canBeHarvested(now)) throw PlantNotHarvestableException()

        /* Store crop */
        val crop: MaterializedItem = when (plant) {
            is MaterializedPlant.AppleTree -> MaterializedItem.Apple(1)
            is MaterializedPlant.TomatoPlant -> MaterializedItem.Tomato(1)
        }
        storeInInventoryReturnItemID(
            transaction, now, userId, crop, ItemHistoryInfo.FirstHarvested(userId), dexTable, inventoryTable, itemHistoryTable, materializedItemTable
        )

        /* Gain farming exp */
        val currentFarmingExp = userStatsTable.getUserStats(userId).farmingExp
        userStatsTable.updateFarmingExp(userId, currentFarmingExp + 2) // TODO: Proper exp system

        if (plant is IMaterializedPlant.Maturable) {
            /* For maturable plants, start new cycle and harvest */
            val harvestedPlant = when (plant) {
                is MaterializedPlant.AppleTree -> plant.copy(cycleStart = now, harvests = plant.harvests + 1)
                else -> throw IllegalStateException()
            }

            materializedPlantTable.setNewPlantCycleAndHarvest(plantId, harvestedPlant.cycleStart, harvestedPlant.harvests)

            return@transaction plot.copy(plant = plantId to harvestedPlant)
        } else {
            /* For non-maturable plants, delete */
            materializedPlantTable.deletePlant(plantId)

            return@transaction plot.copy(plant = null)
        }
    }

    override fun expand(userId: Int): Plot = transaction {
        return@transaction plotTable.createAndGetEmptyPlot(userId)
    }

    override fun reorder(userId: Int, plotId: Long, targetIndex: Int): List<Plot> = transaction {
        plotListTable.reorder(userId, plotId, targetIndex)

        return@transaction getPlotsInOrder(userId)
    }
}
