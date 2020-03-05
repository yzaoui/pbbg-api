package com.bitwiserain.pbbg.db.repository.farm

import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.db.repository.farm.MaterializedPlantTable.toMaterializedPlant
import com.bitwiserain.pbbg.domain.model.farm.Plot
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*

object PlotTable : LongIdTable() {
    val userId = reference("user_id", UserTable)
    val plantId = reference("plant_id", MaterializedPlantTable, ReferenceOption.SET_NULL).nullable()

    fun createAndGetEmptyPlot(userId: Int): Plot = insertAndGetId {
        it[PlotTable.userId] = EntityID(userId, UserTable)
    }.let {
        Plot(it.value, null)
    }

    fun updatePlot(userId: Int, plotId: Long, plantId: Long) = update({ PlotTable.userId.eq(userId) and PlotTable.id.eq(plotId) }) {
        it[PlotTable.plantId] = EntityID(plantId, MaterializedPlantTable)
    }

    fun getPlots(userId: Int): List<Plot> =
        (PlotTable leftJoin MaterializedPlantTable)
            .select { PlotTable.userId.eq(userId) }
            .map { it.toPlot() }

    fun getPlot(userId: Int, plotId: Long) =
        (PlotTable leftJoin MaterializedPlantTable)
            .select { PlotTable.userId.eq(userId) and PlotTable.id.eq(plotId) }
            .singleOrNull()
            ?.toPlot()
}

private fun ResultRow.toPlot(): Plot {
    val plantId = this[PlotTable.plantId]?.value

    return Plot(
        id = this[PlotTable.id].value,
        plant = if (plantId != null) plantId to toMaterializedPlant() else null
    )
}
