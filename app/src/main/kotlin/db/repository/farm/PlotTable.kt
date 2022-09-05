package com.bitwiserain.pbbg.app.db.repository.farm

import com.bitwiserain.pbbg.app.db.repository.UserTableImpl
import com.bitwiserain.pbbg.app.domain.model.farm.Plot
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

interface PlotTable {

    fun createAndGetEmptyPlot(userId: Int): Plot

    fun updatePlot(userId: Int, plotId: Long, plantId: Long)

    fun getPlots(userId: Int): List<Plot>

    fun getPlot(userId: Int, plotId: Long): Plot?
}

class PlotTableImpl : PlotTable {

    object Exposed : LongIdTable(name = "Plot") {

        val userId = reference("user_id", UserTableImpl.Exposed)
        val plantId = reference("plant_id", MaterializedPlantTableImpl.Exposed, ReferenceOption.SET_NULL).nullable()
    }

    override fun createAndGetEmptyPlot(userId: Int): Plot {
        val newPlotId = Exposed.insertAndGetId {
            it[Exposed.userId] = EntityID(userId, UserTableImpl.Exposed)
        }.value

        val currentPlotIdList: List<Long?> = PlotListTableImpl.Exposed
            .select { PlotListTableImpl.Exposed.userId.eq(userId) }
            .single()[PlotListTableImpl.Exposed.plotIdList]
            .let(Json::decodeFromString)

        val updatedPlotIdListJSON = Json.encodeToString(currentPlotIdList + newPlotId)

        PlotListTableImpl.Exposed.update({ PlotListTableImpl.Exposed.userId.eq(userId) }) {
            it[PlotListTableImpl.Exposed.plotIdList] = updatedPlotIdListJSON
        }

        return Plot(newPlotId, null)
    }

    override fun updatePlot(userId: Int, plotId: Long, plantId: Long) {
        Exposed.update({ Exposed.userId.eq(userId) and Exposed.id.eq(plotId) }) {
            it[Exposed.plantId] = EntityID(plantId, MaterializedPlantTableImpl.Exposed)
        }
    }

    override fun getPlots(userId: Int): List<Plot> =
        (Exposed leftJoin MaterializedPlantTableImpl.Exposed)
            .select { Exposed.userId.eq(userId) }
            .map { it.toPlot() }

    override fun getPlot(userId: Int, plotId: Long) =
        (Exposed leftJoin MaterializedPlantTableImpl.Exposed)
            .select { Exposed.userId.eq(userId) and Exposed.id.eq(plotId) }
            .singleOrNull()
            ?.toPlot()


    private fun ResultRow.toPlot(): Plot {
        val plantId = this[Exposed.plantId]?.value

        return Plot(
            id = this[Exposed.id].value,
            plant = if (plantId != null) plantId to toMaterializedPlant() else null
        )
    }
}
