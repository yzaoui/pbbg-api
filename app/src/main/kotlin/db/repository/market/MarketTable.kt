package com.bitwiserain.pbbg.app.db.repository.market

import com.bitwiserain.pbbg.app.db.repository.UserTableImpl
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.insertAndGetId

interface MarketTable {

    fun createMarketAndGetId(userId: Int): Int
}

class MarketTableImpl : MarketTable {

    object Exposed: IntIdTable(name = "Market") {

        val userId = reference("user_id", UserTableImpl.Exposed).uniqueIndex()
    }

    override fun createMarketAndGetId(userId: Int): Int = Exposed.insertAndGetId {
        it[Exposed.userId] = EntityID(userId, UserTableImpl.Exposed)
    }.value
}
