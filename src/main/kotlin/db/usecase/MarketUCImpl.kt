package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.*
import com.bitwiserain.pbbg.db.repository.market.MarketInventoryTable
import com.bitwiserain.pbbg.domain.PriceManager
import com.bitwiserain.pbbg.domain.model.BaseItem
import com.bitwiserain.pbbg.domain.model.MaterializedItem
import com.bitwiserain.pbbg.domain.model.market.Market
import com.bitwiserain.pbbg.domain.model.market.MarketItem
import com.bitwiserain.pbbg.domain.model.market.MarketOrder
import com.bitwiserain.pbbg.domain.model.market.UserAndGameMarkets
import com.bitwiserain.pbbg.domain.usecase.MarketUC
import com.bitwiserain.pbbg.domain.usecase.NotEnoughGoldException
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class MarketUCImpl(private val db: Database) : MarketUC {
    override fun getMarkets(userId: Int): UserAndGameMarkets = transaction(db) {
        val userId = EntityID(userId, UserTable)

        val gold = UserStatsTable.getUserStats(userId).gold

        val userMarket = Market(
            Joins.getInventoryItems(userId).mapValues { MarketItem(it.value.item, PriceManager.getSellPrice(it.value.item)) }
        )

        val gameMarket = Market(
            Joins.Market.getItems(userId).mapValues { MarketItem(it.value, PriceManager.getBuyPrice(it.value)) }
        )

        return@transaction UserAndGameMarkets(gold = gold, userMarket = userMarket, gameMarket = gameMarket)
    }

    override fun buy(userId: Int, orders: List<MarketOrder>): UserAndGameMarkets = transaction(db) {
        val userId = EntityID(userId, UserTable)

        var gold = UserStatsTable.getUserStats(userId).gold
        val userMarket = Joins.getInventoryItems(userId).toMutableMap()
        val gameMarket = Joins.Market.getItems(userId).toMutableMap()

        val userItemsToInsert = mutableMapOf<Long, BaseItem>()
        val userStackableItemsToCreate = mutableListOf<MaterializedItem.Stackable>()
        val userItemsToUpdateQuantity = mutableMapOf<Long, Int>()
        val gameItemsToRemove = mutableSetOf<Long>()
        val gameItemsToUpdateQuantity = mutableMapOf<Long, Int>()

        for (order in orders) {
            /* Ordered item must exist in game market */
            val gameItem = gameMarket[order.id] ?: throw Exception()

            /* Order quantity exists iff item is stackable */
            if ((gameItem is MaterializedItem.Stackable && order.quantity == null) || (gameItem !is MaterializedItem.Stackable && order.quantity != null)) {
                throw Exception()
            }

            if (order.quantity !== null) {
                gameItem as MaterializedItem.Stackable

                /* Ordered item quantity must be non-negative */
                if (order.quantity < 0) throw Exception()
                /* Ordered item quantity must not exceed game item quantity */
                if (order.quantity > gameItem.quantity) throw Exception()
                /* Skip transaction if quantity is 0 */
                if (order.quantity == 0) continue

                /** Handle user-side of item **/
                /* Check if user has this type of stackable item already */
                val heldItemOfThisKind = userMarket.entries.find { it.value.base === gameItem.base }
                if (heldItemOfThisKind != null) {
                    userItemsToUpdateQuantity[heldItemOfThisKind.key] = +order.quantity
                } else {
                    userStackableItemsToCreate.add((gameItem as MaterializedItem.Stackable).copy(order.quantity))
                }

                /** Handle game-side of item **/
                if (order.quantity == gameItem.quantity) {
                    gameItemsToRemove.add(order.id)
                } else {
                    gameItemsToUpdateQuantity[order.id] = -order.quantity
                }

                gold -= PriceManager.getBuyPrice(gameItem) * order.quantity
            } else {
                /** For non-stackable items, just transfer **/
                userItemsToInsert[order.id] = gameItem.base
                gameItemsToRemove.add(order.id)

                gold -= PriceManager.getBuyPrice(gameItem)
            }
        }

        if (gold < 0) throw NotEnoughGoldException()

        /* Update user gold */
        UserStatsTable.updateGold(userId, gold)

        /* Create new materialized items */
        userStackableItemsToCreate.forEach {
            val item = it as MaterializedItem

            userItemsToInsert[MaterializedItemTable.insertItemAndGetId(item).value] = item.base
        }
        /* Insert items into user's inventory */
        InventoryTable.insertItems(userId, userItemsToInsert)
        /* Update quantity of user's items */
        userItemsToUpdateQuantity.forEach { MaterializedItemTable.updateQuantity(it.key, it.value) }
        /* Remove game's items */
        MarketInventoryTable.removeItems(gameItemsToRemove)
        /* Update quantity of game's items */
        gameItemsToUpdateQuantity.forEach { MaterializedItemTable.updateQuantity(it.key, it.value) }

        return@transaction UserAndGameMarkets(
            gold = gold,
            userMarket = Market(
                Joins.getInventoryItems(userId)
                    .mapValues { MarketItem(it.value.item, PriceManager.getSellPrice(it.value.item)) }
            ),
            gameMarket = Market(
                Joins.Market.getItems(userId)
                    .mapValues { MarketItem(it.value, PriceManager.getBuyPrice(it.value)) }
            )
        )
    }

    override fun sell(userId: Int, orders: List<MarketOrder>): UserAndGameMarkets = transaction(db) {
        val userId = EntityID(userId, UserTable)

        var gold = UserStatsTable.getUserStats(userId).gold
        val userMarket = Joins.getInventoryItems(userId).toMutableMap()
        val gameMarket = Joins.Market.getItems(userId).toMutableMap()

        val gameItemsToInsert = mutableSetOf<Long>()
        val gameStackableItemsToCreate = mutableListOf<MaterializedItem.Stackable>()
        val gameItemsToUpdateQuantity = mutableMapOf<Long, Int>()
        val userItemsToRemove = mutableSetOf<Long>()
        val userItemsToUpdateQuantity = mutableMapOf<Long, Int>()

        for (order in orders) {
            /* Ordered item must exist in user inventory */
            val userItem = userMarket[order.id]?.item ?: throw Exception()

            /* Order quantity exists iff item is stackable */
            if ((userItem is MaterializedItem.Stackable && order.quantity == null) || (userItem !is MaterializedItem.Stackable && order.quantity != null)) {
                throw Exception()
            }

            if (order.quantity !== null) {
                userItem as MaterializedItem.Stackable

                /* Ordered item quantity must be non-negative */
                if (order.quantity < 0) throw Exception()
                /* Ordered item quantity must not exceed user item quantity */
                if (order.quantity > userItem.quantity) throw Exception()
                /* Skip transaction if quantity is 0 */
                if (order.quantity == 0) continue

                /** Handle game-side of item **/
                /* Check if game has this type of stackable item already */
                val gameItemOfThisKind = gameMarket.entries.find { it.value.base === userItem.base }
                if (gameItemOfThisKind != null) {
                    gameItemsToUpdateQuantity[gameItemOfThisKind.key] = +order.quantity
                } else {
                    gameStackableItemsToCreate.add((userItem as MaterializedItem.Stackable).copy(order.quantity))
                }

                /** Handle user-side of item **/
                if (order.quantity == userItem.quantity) {
                    userItemsToRemove.add(order.id)
                } else {
                    userItemsToUpdateQuantity[order.id] = -order.quantity
                }

                gold += PriceManager.getSellPrice(userItem) * order.quantity
            } else {
                /** For non-stackable items, just transfer **/
                gameItemsToInsert.add(order.id)
                userItemsToRemove.add(order.id)

                gold += PriceManager.getSellPrice(userItem)
            }
        }

        /* Update user gold */
        UserStatsTable.updateGold(userId, gold)

        /* Create new materialized items */
        gameStackableItemsToCreate.forEach {
            val item = it as MaterializedItem

            gameItemsToInsert.add(MaterializedItemTable.insertItemAndGetId(item).value)
        }
        /* Insert items into game's market */
        Joins.Market.insertItems(userId, gameItemsToInsert)
        /* Update quantity of game's items */
        gameItemsToUpdateQuantity.forEach { MaterializedItemTable.updateQuantity(it.key, it.value) }
        /* Remove user's items */
        InventoryTable.removeItems(userId, userItemsToRemove)
        /* Update quantity of user's items */
        userItemsToUpdateQuantity.forEach { MaterializedItemTable.updateQuantity(it.key, it.value) }

        return@transaction UserAndGameMarkets(
            gold = gold,
            userMarket = Market(
                Joins.getInventoryItems(userId)
                    .mapValues { MarketItem(it.value.item, PriceManager.getSellPrice(it.value.item)) }
            ),
            gameMarket = Market(
                Joins.Market.getItems(userId)
                    .mapValues { MarketItem(it.value, PriceManager.getBuyPrice(it.value)) }
            )
        )
    }
}
