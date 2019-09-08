package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.InventoryTable
import com.bitwiserain.pbbg.db.repository.Joins
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.db.repository.market.MarketInventoryTable
import com.bitwiserain.pbbg.domain.PriceManager
import com.bitwiserain.pbbg.domain.model.BaseItem
import com.bitwiserain.pbbg.domain.model.market.Market
import com.bitwiserain.pbbg.domain.model.market.MarketItem
import com.bitwiserain.pbbg.domain.model.market.MarketOrder
import com.bitwiserain.pbbg.domain.model.market.UserAndGameMarkets
import com.bitwiserain.pbbg.domain.usecase.MarketUC
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class MarketUCImpl(private val db: Database) : MarketUC {
    override fun getMarkets(userId: Int): UserAndGameMarkets = transaction(db) {
        val userId = EntityID(userId, UserTable)

        val userMarket = Market(
            Joins.getInventoryItems(userId).mapValues { MarketItem(it.value.item, PriceManager.getSellPrice(it.value.item)) }
        )

        val gameMarket = Market(
            Joins.Market.getItems(userId).mapValues { MarketItem(it.value, PriceManager.getBuyPrice(it.value)) }
        )

        return@transaction UserAndGameMarkets(userMarket = userMarket, gameMarket = gameMarket)
    }

    override fun buy(userId: Int, orders: List<MarketOrder>): UserAndGameMarkets = transaction(db) {
        val userId = EntityID(userId, UserTable)

        val marketInventory = Joins.Market.getItems(userId).toMutableMap()

        var totalPrice = 0
        val itemIds = mutableMapOf<Long, BaseItem>()
        for (order in orders) {
            val item = marketInventory[order.id] ?: continue

            totalPrice += PriceManager.getBuyPrice(item)
            itemIds[order.id] = item.base
            marketInventory.remove(order.id)
        }

        InventoryTable.insertItems(userId, itemIds)
        MarketInventoryTable.removeItems(itemIds.keys)

        val userInventory = Joins.getInventoryItems(userId)
            .mapValues { MarketItem(it.value.item, PriceManager.getSellPrice(it.value.item)) }

        return@transaction UserAndGameMarkets(
            userMarket = Market(userInventory),
            gameMarket = Market(marketInventory
                .mapValues { MarketItem(it.value, PriceManager.getBuyPrice(it.value)) }
            )
        )
    }

    override fun sell(userId: Int, orders: List<MarketOrder>): UserAndGameMarkets = transaction(db) {
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
        Joins.Market.insertItems(userId, itemIds)

        return@transaction UserAndGameMarkets(
            userMarket = Market(heldItems.mapValues { MarketItem(it.value.item, PriceManager.getSellPrice(it.value.item)) }),
            gameMarket = Market(Joins.Market.getItems(userId).mapValues { MarketItem(it.value, PriceManager.getBuyPrice(it.value)) })
        )
    }
}
