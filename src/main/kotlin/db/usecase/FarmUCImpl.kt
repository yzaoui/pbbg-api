package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.DexTable
import com.bitwiserain.pbbg.db.repository.InventoryTable
import com.bitwiserain.pbbg.db.repository.ItemHistoryTable
import com.bitwiserain.pbbg.db.repository.Joins
import com.bitwiserain.pbbg.db.repository.MaterializedItemTable
import com.bitwiserain.pbbg.db.repository.UserStatsTable
import com.bitwiserain.pbbg.db.repository.farm.MaterializedPlantTable
import com.bitwiserain.pbbg.db.repository.farm.MaterializedPlantTable.PlantForm
import com.bitwiserain.pbbg.db.repository.farm.PlotTable
import com.bitwiserain.pbbg.domain.model.BaseItem
import com.bitwiserain.pbbg.domain.model.MaterializedItem
import com.bitwiserain.pbbg.domain.model.farm.IBasePlant
import com.bitwiserain.pbbg.domain.model.farm.IMaterializedPlant
import com.bitwiserain.pbbg.domain.model.farm.MaterializedPlant
import com.bitwiserain.pbbg.domain.model.farm.Plot
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistoryInfo
import com.bitwiserain.pbbg.domain.usecase.EmptyPlotException
import com.bitwiserain.pbbg.domain.usecase.FarmUC
import com.bitwiserain.pbbg.domain.usecase.InsufficientItemQuantity
import com.bitwiserain.pbbg.domain.usecase.InventoryItemNotFoundException
import com.bitwiserain.pbbg.domain.usecase.ItemNotPlantableException
import com.bitwiserain.pbbg.domain.usecase.OccupiedPlotException
import com.bitwiserain.pbbg.domain.usecase.PlantNotHarvestableException
import com.bitwiserain.pbbg.domain.usecase.UserPlotNotFoundException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Clock

class FarmUCImpl(
    private val db: Database,
    private val clock: Clock,
    private val dexTable: DexTable,
    private val itemHistoryTable: ItemHistoryTable,
    private val materializedPlantTable: MaterializedPlantTable,
    private val plotTable: PlotTable
) : FarmUC {

    override fun getPlots(userId: Int): List<Plot> = transaction(db) {
        return@transaction plotTable.getPlots(userId)
    }

    override fun plant(userId: Int, plotId: Long, itemId: Long): Plot = transaction(db) {
        val now = clock.instant()

        /* Make sure user owns this plot */
        val plot = plotTable.getPlot(userId, plotId) ?: throw UserPlotNotFoundException()

        /* Make sure plot is empty */
        if (plot.plant != null) throw OccupiedPlotException()

        /* Make sure user owns this item */
        val item = (Joins.getInventoryItem(userId, itemId) ?: throw InventoryItemNotFoundException(itemId)).item
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
            MaterializedItemTable.updateQuantity(itemId, -1)
        } else {
            InventoryTable.removeItem(userId, itemId)
        }

        return@transaction plotTable.getPlot(userId, plot.id)!!
    }

    override fun harvest(userId: Int, plotId: Long): Plot = transaction(db) {
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
        storeInInventoryReturnItemID(db, now, userId, crop, ItemHistoryInfo.FirstHarvested(userId), dexTable, itemHistoryTable)

        /* Gain farming exp */
        val currentFarmingExp = UserStatsTable.getUserStats(userId).farmingExp
        UserStatsTable.updateFarmingExp(userId, currentFarmingExp + 2) // TODO: Proper exp system

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

    override fun expand(userId: Int): Plot = transaction(db) {
        return@transaction plotTable.createAndGetEmptyPlot(userId)
    }
}
