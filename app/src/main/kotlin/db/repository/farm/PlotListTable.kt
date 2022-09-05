package com.bitwiserain.pbbg.app.db.repository.farm

import com.bitwiserain.pbbg.app.db.repository.UserTableImpl
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

interface PlotListTable {
    fun insertUser(userId: Int)

    fun get(userId: Int): List<Long>
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
}
