package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.Joins
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.db.repository.farm.MaterializedPlantTable
import com.bitwiserain.pbbg.db.repository.farm.MaterializedPlantTable.PlantForm
import com.bitwiserain.pbbg.db.repository.farm.PlotTable
import com.bitwiserain.pbbg.domain.model.BaseItem
import com.bitwiserain.pbbg.domain.model.farm.IBasePlant
import com.bitwiserain.pbbg.domain.model.farm.IMaterializedPlant
import com.bitwiserain.pbbg.domain.model.farm.MaterializedPlant
import com.bitwiserain.pbbg.domain.model.farm.Plot
import com.bitwiserain.pbbg.domain.usecase.*
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Clock

class FarmUCImpl(private val db: Database, private val clock: Clock) : FarmUC {
    override fun getPlots(userId: Int): List<Plot> = transaction(db) {
        return@transaction PlotTable.getPlots(userId)
    }

    override fun plant(userId: Int, plotId: Long, itemId: Long): Plot = transaction(db) {
        val now = clock.instant()

        /* Make sure user owns this plot */
        val plot = PlotTable.getPlot(userId, plotId) ?: throw UserPlotNotFoundException()

        /* Make sure plot is empty */
        if (plot.plant != null) throw OccupiedPlotException()

        /* Make sure user owns this item */
        val baseItem = (Joins.getInventoryItem(EntityID(userId, UserTable), itemId) ?: throw InventoryItemNotFoundException(itemId)).base

        /* Make sure item can be planted */
        if (baseItem !is BaseItem.Plantable) throw ItemNotPlantableException()

        /* Create plant in database */
        val plantId = MaterializedPlantTable.insertPlantAndGetId(PlantForm(
            enum = baseItem.basePlant.enum,
            cycleStart = now,
            isMaturable = baseItem.basePlant is IBasePlant.Maturable
        )).value

        PlotTable.updatePlot(userId, plot.id, plantId)

        return@transaction PlotTable.getPlot(userId, plot.id)!!
    }

    override fun harvest(userId: Int, plotId: Long): Plot = transaction(db) {
        val now = clock.instant()

        /* Make sure user owns this plot */
        val plot = PlotTable.getPlot(userId, plotId) ?: throw UserPlotNotFoundException()

        /* Make sure plot is not empty */
        val (plantId, plant) = plot.plant ?: throw EmptyPlotException()

        /* Make sure plant can be harvested */
        if (!plant.canBeHarvested(now)) throw PlantNotHarvestableException()

        if (plant is IMaterializedPlant.Maturable) {
            /* For maturable plants, start new cycle and harvest */
            val harvestedPlant = when (plant) {
                is MaterializedPlant.AppleTree -> plant.copy(cycleStart = now)
                else -> throw IllegalStateException()
            }

            MaterializedPlantTable.setNewPlantCycleAndHarvest(plantId, harvestedPlant.cycleStart)

            return@transaction plot.copy(plant = plantId to harvestedPlant)
        } else {
            /* For non-maturable plants, delete */
            MaterializedPlantTable.deletePlant(plantId)

            return@transaction plot.copy(plant = null)
        }
    }

    override fun expand(userId: Int): Plot = transaction(db) {
        return@transaction PlotTable.createAndGetEmptyPlot(userId)
    }
}
