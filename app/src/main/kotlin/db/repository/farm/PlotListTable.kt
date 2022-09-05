package com.bitwiserain.pbbg.app.db.repository.farm

import com.bitwiserain.pbbg.app.db.repository.UserTableImpl
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert

interface PlotListTable {
    fun insertUser(userId: Int)
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
}
