package com.bitwiserain.pbbg.db.repository.market

import com.bitwiserain.pbbg.db.repository.UserTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.insertAndGetId

interface MarketTable {

    fun createMarketAndGetId(userId: Int): Int
}

class MarketTableImpl : MarketTable {

    object Exposed: IntIdTable(name = "Market") {

        val userId = reference("user_id", UserTable).uniqueIndex()
    }

    override fun createMarketAndGetId(userId: Int): Int = Exposed.insertAndGetId {
        it[Exposed.userId] = EntityID(userId, UserTable)
    }.value
}
