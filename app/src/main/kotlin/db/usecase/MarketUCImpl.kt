package com.bitwiserain.pbbg.app.db.usecase

import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.DexTable
import com.bitwiserain.pbbg.app.db.repository.InventoryTable
import com.bitwiserain.pbbg.app.db.repository.MaterializedItemTable
import com.bitwiserain.pbbg.app.db.repository.UserStatsTable
import com.bitwiserain.pbbg.app.db.repository.market.MarketInventoryTable
import com.bitwiserain.pbbg.app.domain.PriceManager
import com.bitwiserain.pbbg.app.domain.model.BaseItem
import com.bitwiserain.pbbg.app.domain.model.ItemEnum
import com.bitwiserain.pbbg.app.domain.model.MaterializedItem
import com.bitwiserain.pbbg.app.domain.model.market.Market
import com.bitwiserain.pbbg.app.domain.model.market.MarketItem
import com.bitwiserain.pbbg.app.domain.model.market.MarketOrder
import com.bitwiserain.pbbg.app.domain.model.market.UserAndGameMarkets
import com.bitwiserain.pbbg.app.domain.usecase.MarketUC
import com.bitwiserain.pbbg.app.domain.usecase.NotEnoughGoldException

class MarketUCImpl(
    private val transaction: Transaction,
    private val dexTable: DexTable,
    private val inventoryTable: InventoryTable,
    private val marketInventoryTable: MarketInventoryTable,
    private val materializedItemTable: MaterializedItemTable,
    private val userStatsTable: UserStatsTable,
) : MarketUC {

    override fun getMarkets(userId: Int): UserAndGameMarkets = transaction {
        val gold = userStatsTable.getUserStats(userId).gold

        val userMarket = Market(
            inventoryTable.getInventoryItems(userId)
                .filterOutZeroQuantityItems()
                .mapValues { MarketItem(it.value.item, PriceManager.getSellPrice(it.value.item)) }
        )

        val gameMarket = Market(
            marketInventoryTable.getItems(userId)
                .filterOutZeroQuantityItems()
                .mapValues { MarketItem(it.value, PriceManager.getBuyPrice(it.value)) }
        )

        return@transaction UserAndGameMarkets(gold = gold, userMarket = userMarket, gameMarket = gameMarket)
    }

    override fun buy(userId: Int, orders: List<MarketOrder>): UserAndGameMarkets = transaction {
        var gold = userStatsTable.getUserStats(userId).gold
        val userMarket = inventoryTable.getInventoryItems(userId).toMutableMap()
        val gameMarket = marketInventoryTable.getItems(userId).toMutableMap()
        val dex = dexTable.getDiscovered(userId)

        val userItemsToInsert = mutableMapOf<Long, BaseItem>()
        val userStackableItemsToCreate = mutableListOf<MaterializedItem.Stackable>()
        val userItemsToUpdateQuantity = mutableMapOf<Long, Int>()
        val dexItemsToInsert = mutableSetOf<ItemEnum>()
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

            /* Insert into dex if not already there */
            if (!dex.contains(gameItem.enum)) dexItemsToInsert.add(gameItem.enum)
        }

        if (gold < 0) throw NotEnoughGoldException()

        /* Update user gold */
        userStatsTable.updateGold(userId, gold)

        /* Create new materialized items */
        userStackableItemsToCreate.forEach {
            val item = it as MaterializedItem

            userItemsToInsert[materializedItemTable.insertItemAndGetId(item)] = item.base
        }
        /* Insert items into user's inventory */
        inventoryTable.insertItems(userId, userItemsToInsert)
        /* Insert items into user's dex */
        dexTable.insertDiscovered(userId, dexItemsToInsert)
        /* Update quantity of user's items */
        userItemsToUpdateQuantity.forEach { materializedItemTable.updateQuantity(it.key, it.value) }
        /* Remove game's items */
        marketInventoryTable.removeItems(gameItemsToRemove)
        /* Update quantity of game's items */
        gameItemsToUpdateQuantity.forEach { materializedItemTable.updateQuantity(it.key, it.value) }

        return@transaction UserAndGameMarkets(
            gold = gold,
            userMarket = Market(
                inventoryTable.getInventoryItems(userId)
                    .filterOutZeroQuantityItems()
                    .mapValues { MarketItem(it.value.item, PriceManager.getSellPrice(it.value.item)) }
            ),
            gameMarket = Market(
                marketInventoryTable.getItems(userId)
                    .filterOutZeroQuantityItems()
                    .mapValues { MarketItem(it.value, PriceManager.getBuyPrice(it.value)) }
            )
        )
    }

    override fun sell(userId: Int, orders: List<MarketOrder>): UserAndGameMarkets = transaction {
        var gold = userStatsTable.getUserStats(userId).gold
        val userMarket = inventoryTable.getInventoryItems(userId).toMutableMap()
        val gameMarket = marketInventoryTable.getItems(userId).toMutableMap()

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
                userItemsToUpdateQuantity[order.id] = -order.quantity

                gold += PriceManager.getSellPrice(userItem) * order.quantity
            } else {
                /** For non-stackable items, just transfer **/
                gameItemsToInsert.add(order.id)
                userItemsToRemove.add(order.id)

                gold += PriceManager.getSellPrice(userItem)
            }
        }

        /* Update user gold */
        userStatsTable.updateGold(userId, gold)

        /* Create new materialized items */
        gameStackableItemsToCreate.forEach {
            val item = it as MaterializedItem

            gameItemsToInsert.add(materializedItemTable.insertItemAndGetId(item))
        }
        /* Insert items into game's market */
        marketInventoryTable.insertItems(userId, gameItemsToInsert)
        /* Update quantity of game's items */
        gameItemsToUpdateQuantity.forEach { materializedItemTable.updateQuantity(it.key, it.value) }
        /* Remove user's items */
        inventoryTable.removeItems(userId, userItemsToRemove)
        /* Update quantity of user's items */
        userItemsToUpdateQuantity.forEach { materializedItemTable.updateQuantity(it.key, it.value) }

        return@transaction UserAndGameMarkets(
            gold = gold,
            userMarket = Market(
                inventoryTable.getInventoryItems(userId)
                    .filterOutZeroQuantityItems()
                    .mapValues { MarketItem(it.value.item, PriceManager.getSellPrice(it.value.item)) }
            ),
            gameMarket = Market(
                marketInventoryTable.getItems(userId)
                    .filterOutZeroQuantityItems()
                    .mapValues { MarketItem(it.value, PriceManager.getBuyPrice(it.value)) }
            )
        )
    }
}

private fun Map<Long, MaterializedItem>.filterOutZeroQuantityItems() = filter {
    it.value.let { it !is MaterializedItem.Stackable || it.quantity > 0 }
}
