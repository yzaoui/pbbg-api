package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.InventoryTable
import com.bitwiserain.pbbg.db.repository.Joins
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.PriceManager
import com.bitwiserain.pbbg.domain.model.InventoryItem
import com.bitwiserain.pbbg.domain.model.MaterializedItem
import com.bitwiserain.pbbg.domain.model.market.Market
import com.bitwiserain.pbbg.domain.model.market.MarketItem
import com.bitwiserain.pbbg.domain.model.market.MarketOrder
import com.bitwiserain.pbbg.domain.usecase.MarketUC
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class MarketUCImpl(private val db: Database) : MarketUC {
    override fun getMarket(userId: Int): Market = transaction(db) {
        val userId = EntityID(userId, UserTable)

        val marketItems = Joins.getMarketItems(userId)
            .mapValues { MarketItem(it.value, PriceManager.getBuyPrice(it.value)) }

        return@transaction Market(marketItems)
    }

    override fun getUserInventory(userId: Int): Market = transaction(db) {
        val userId = EntityID(userId, UserTable)

        val marketItems = Joins.getInventoryItems(userId)
            .mapValues { MarketItem(it.value.item, PriceManager.getSellPrice(it.value.item)) }

        return@transaction Market(marketItems)
    }

    override fun sell(userId: Int, orders: List<MarketOrder>): Market = transaction(db) {
        val userId = EntityID(userId, UserTable)

        val heldItems = Joins.getInventoryItems(userId).toMutableMap()

        var totalPrice = 0
        val itemIds = mutableListOf<Long>()
        for (order in orders) {
            val item = heldItems[order.id] ?: continue

            totalPrice += PriceManager.getSellPrice(item.item)
            itemIds.add(order.id)
            heldItems.remove(order.id)
        }

        InventoryTable.removeItems(userId, itemIds)

        return@transaction Market(heldItems.mapValues { MarketItem(it.value.item, PriceManager.getSellPrice(it.value.item)) })
    }
}
