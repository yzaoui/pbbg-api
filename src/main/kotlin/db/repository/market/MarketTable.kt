package com.bitwiserain.pbbg.db.repository.market

import com.bitwiserain.pbbg.db.repository.UserTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.insertAndGetId

object MarketTable: IntIdTable() {
    val userId = reference("user_id", UserTable).uniqueIndex()

    fun createMarketAndGetId(userId: Int): Int = insertAndGetId {
        it[MarketTable.userId] = EntityID(userId, UserTable)
    }.value
}
