package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.db.repository.market.MarketInventoryTable
import com.bitwiserain.pbbg.db.repository.market.MarketTable
import com.bitwiserain.pbbg.domain.model.InventoryItem
import com.bitwiserain.pbbg.domain.model.ItemEnum
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

object Joins {
    fun getHeldItemsOfBaseKind(userId: Int, itemEnum: ItemEnum) =
        (InventoryTable innerJoin MaterializedItemTable)
            .select { InventoryTable.userId.eq(userId) and MaterializedItemTable.itemEnum.eq(itemEnum) }
            .associate { it[MaterializedItemTable.id].value to it.toMaterializedItem() }

    fun getInventoryItems(userId: Int): Map<Long, InventoryItem> =
        (InventoryTable innerJoin MaterializedItemTable)
            .select { InventoryTable.userId.eq(userId) }
            .associate { it[MaterializedItemTable.id].value to it.toInventoryItem() }

    fun getInventoryItem(userId: Int, itemId: Long): InventoryItem? =
        (InventoryTable innerJoin MaterializedItemTable)
            .select { InventoryTable.userId.eq(userId) and InventoryTable.materializedItem.eq(itemId) }
            .singleOrNull()
            ?.toInventoryItem()

    fun setItemEquipped(userId: Int, itemId: Long, equipped: Boolean) =
        InventoryTable.update({ InventoryTable.userId.eq(userId) and InventoryTable.materializedItem.eq(itemId) }) {
            it[InventoryTable.equipped] = equipped
        }

    object Market {
        private fun getMarketId(userId: Int) =
            MarketTable.select { MarketTable.userId.eq(userId) }
                .map { it[MarketTable.id] }
                .single()
                .value

        fun getItems(userId: Int) =
            (MarketInventoryTable innerJoin MaterializedItemTable innerJoin MarketTable)
                .select { MarketTable.userId.eq(userId) }
                .associate { it[MaterializedItemTable.id].value to it.toMaterializedItem() }

        fun insertItems(userId: Int, itemIds: Iterable<Long>) {
            val marketId = getMarketId(userId)

            MarketInventoryTable.batchInsert(itemIds) { itemid ->
                this[MarketInventoryTable.marketId] = EntityID(marketId, MarketTable)
                this[MarketInventoryTable.materializedItem] = EntityID(itemid, MaterializedItemTable)
            }
        }
    }
}

