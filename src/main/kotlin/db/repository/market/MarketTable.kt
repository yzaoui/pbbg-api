package com.bitwiserain.pbbg.db.repository.market

import com.bitwiserain.pbbg.db.repository.UserTable
import org.jetbrains.exposed.dao.IntIdTable

object MarketTable: IntIdTable() {
    val userId = reference("user_id", UserTable).uniqueIndex()
}
