package com.bitwiserain.pbbg.app.db.repository.farm

import com.bitwiserain.pbbg.app.db.repository.UserTableImpl
import com.bitwiserain.pbbg.app.reorder
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

interface PlotListTable {
    fun insertUser(userId: Int)

    fun get(userId: Int): List<Long>

    fun reorder(userId: Int, plotId: Long, newIndex: Int)
}

class PlotListTableImpl : PlotListTable {
    object Exposed : Table("PlotList") {
        val userId = reference("user_id", UserTableImpl.Exposed)
        val plotIdList = text("plot_id_list") // List<Long>
    }

    override fun insertUser(userId: Int) {
        Exposed.insert {
            it[Exposed.userId] = userId
            it[Exposed.plotIdList] = Json.encodeToString(emptyList<Long>())
        }
    }

    override fun get(userId: Int): List<Long> = Exposed
        .select { Exposed.userId.eq(userId) }
        .single()[Exposed.plotIdList]
        .let(Json::decodeFromString)

    override fun reorder(userId: Int, plotId: Long, newIndex: Int) {
        val plotIdList: List<Long> = Exposed.select { Exposed.userId.eq(userId) }.single()[Exposed.plotIdList]
            .let(Json::decodeFromString)

        if (newIndex !in plotIdList.indices) throw IllegalArgumentException()

        val oldIndex = plotIdList.indexOfFirst { it == plotId }
        if (oldIndex == -1) throw IllegalStateException()

        // No need to do any work
        if (oldIndex == newIndex) return

        val newPlotIdListJSON = plotIdList.reorder(fromIndex = oldIndex, toIndex = newIndex).let(Json::encodeToString)

        Exposed.update({ Exposed.userId.eq(userId) }) {
            it[Exposed.plotIdList] = newPlotIdListJSON
        }
    }
}
