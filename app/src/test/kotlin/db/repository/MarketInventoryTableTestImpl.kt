package com.bitwiserain.pbbg.app.test.db.repository

import com.bitwiserain.pbbg.app.db.repository.market.MarketInventoryTable
import com.bitwiserain.pbbg.app.domain.model.MaterializedItem

class MarketInventoryTableTestImpl(
    private val items: MutableMap<Long, MaterializedItem> = mutableMapOf(),
    private val marketIds: MutableMap<Int, Int> = mutableMapOf(),
    private val marketInventory: MutableMap<Int, Set<Long>> = mutableMapOf(),
) : MarketInventoryTable {

    override fun getItems(userId: Int): Map<Long, MaterializedItem> {
        val marketId = marketIds[userId] ?: return emptyMap()

        val itemIds: Set<Long> = marketInventory[marketId] ?: return emptyMap()

        return items.filterKeys { it in itemIds }
    }

    override fun insertItems(userId: Int, itemIds: Iterable<Long>) {
        val marketId = marketIds[userId]!!

        marketInventory.compute(marketId) { _, items ->
            (items ?: emptySet()) + itemIds
        }
    }

    override fun insertItem(marketId: Int, itemId: Long) {
        marketInventory.compute(marketId) { _, items ->
            (items ?: emptySet()) + itemId
        }
    }

    override fun removeItems(itemIds: Set<Long>) {
        marketInventory.mapValues {
            it.value - itemIds
        }
    }
}
