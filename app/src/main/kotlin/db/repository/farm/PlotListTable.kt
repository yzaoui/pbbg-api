package com.bitwiserain.pbbg.app.db.repository.farm

import com.bitwiserain.pbbg.app.db.repository.UserTableImpl
import org.jetbrains.exposed.sql.Table

interface PlotListTable

class PlotListTableImpl : PlotListTable {
    object Exposed : Table("PlotList") {
        val userId = reference("user_id", UserTableImpl.Exposed)
        val plotIdList = text("plot_id_list") // List<Long>
    }
}
