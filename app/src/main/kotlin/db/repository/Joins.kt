package com.bitwiserain.pbbg.app.db.repository

import com.bitwiserain.pbbg.app.db.repository.market.MarketInventoryTableImpl
import com.bitwiserain.pbbg.app.db.repository.market.MarketTableImpl
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.select

object Joins {

    object Market {
        private fun getMarketId(userId: Int) =
            MarketTableImpl.Exposed.select { MarketTableImpl.Exposed.userId.eq(userId) }
                .map { it[MarketTableImpl.Exposed.id] }
                .single()
                .value

        fun getItems(userId: Int) =
            (MarketInventoryTableImpl.Exposed innerJoin MaterializedItemTableImpl.Exposed innerJoin MarketTableImpl.Exposed)
                .select { MarketTableImpl.Exposed.userId.eq(userId) }
                .associate { it[MaterializedItemTableImpl.Exposed.id].value to it.toMaterializedItem() }

        fun insertItems(userId: Int, itemIds: Iterable<Long>) {
            val marketId = getMarketId(userId)

            MarketInventoryTableImpl.Exposed.batchInsert(itemIds) { itemid ->
                this[MarketInventoryTableImpl.Exposed.marketId] = EntityID(marketId, MarketTableImpl.Exposed)
                this[MarketInventoryTableImpl.Exposed.materializedItem] = EntityID(itemid, MaterializedItemTableImpl.Exposed)
            }
        }
    }
}

