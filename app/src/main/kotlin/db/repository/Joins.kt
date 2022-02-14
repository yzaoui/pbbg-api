package com.bitwiserain.pbbg.app.db.repository

import com.bitwiserain.pbbg.app.db.repository.market.MarketInventoryTableImpl
import com.bitwiserain.pbbg.app.db.repository.market.MarketTableImpl
import com.bitwiserain.pbbg.app.domain.model.InventoryItem
import com.bitwiserain.pbbg.app.domain.model.ItemEnum
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

object Joins {
    fun getHeldItemsOfBaseKind(userId: Int, itemEnum: ItemEnum) =
        (InventoryTableImpl.Exposed innerJoin MaterializedItemTableImpl.Exposed)
            .select { InventoryTableImpl.Exposed.userId.eq(userId) and MaterializedItemTableImpl.Exposed.itemEnum.eq(itemEnum) }
            .associate { it[MaterializedItemTableImpl.Exposed.id].value to it.toMaterializedItem() }

    fun getInventoryItems(userId: Int): Map<Long, InventoryItem> =
        (InventoryTableImpl.Exposed innerJoin MaterializedItemTableImpl.Exposed)
            .select { InventoryTableImpl.Exposed.userId.eq(userId) }
            .associate { it[MaterializedItemTableImpl.Exposed.id].value to it.toInventoryItem() }

    fun getInventoryItem(userId: Int, itemId: Long): InventoryItem? =
        (InventoryTableImpl.Exposed innerJoin MaterializedItemTableImpl.Exposed)
            .select { InventoryTableImpl.Exposed.userId.eq(userId) and InventoryTableImpl.Exposed.materializedItem.eq(itemId) }
            .singleOrNull()
            ?.toInventoryItem()

    fun setItemEquipped(userId: Int, itemId: Long, equipped: Boolean) =
        InventoryTableImpl.Exposed.update({ InventoryTableImpl.Exposed.userId.eq(userId) and InventoryTableImpl.Exposed.materializedItem.eq(itemId) }) {
            it[InventoryTableImpl.Exposed.equipped] = equipped
        }

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

