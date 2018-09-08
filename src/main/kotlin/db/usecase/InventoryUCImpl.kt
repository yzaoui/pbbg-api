package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.InventoryTable
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.model.*
import com.bitwiserain.pbbg.domain.model.ItemEnum.*
import com.bitwiserain.pbbg.domain.usecase.InventoryUC
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class InventoryUCImpl(private val db: Database) : InventoryUC {
    override fun getInventory(userId: Int): Inventory = transaction(db) {
        // TODO: Consider checking if user exists

        InventoryTable.select { InventoryTable.userId.eq(userId) }
            .map { it.toItem() }
    }

    override fun storeInInventory(userId: Int, item: Item): Unit = transaction(db) {
        // TODO: Consider checking if user exists
        val storedItem = InventoryTable.select { InventoryTable.userId.eq(userId) and InventoryTable.item.eq(item.enum) }
            .map { it.toItem() }
            .singleOrNull()

        if (storedItem != null) {
            // If this kind of item is already stored, and it can be stacked, increase its quantity
            if (item is Stackable) {
                InventoryTable.update({ InventoryTable.userId.eq(userId) and InventoryTable.item.eq(item.enum) }) { updateStatement ->
                    with (SqlExpressionBuilder) {
                        updateStatement.update(InventoryTable.quantity, InventoryTable.quantity + item.quantity)
                    }
                }
            }
        } else {
            // If this item isn't already stored, create an entry for it
            InventoryTable.insert {
                it[InventoryTable.userId] = EntityID(userId, UserTable)
                it[InventoryTable.item] = item.enum
                if (item is Stackable) {
                    it[InventoryTable.quantity] = item.quantity
                }
            }
        }
    }

    private fun ResultRow.toItem(): Item {
        val itemEnum = this[InventoryTable.item]
        val quantity = this[InventoryTable.quantity]

        // TODO: Find a way to preserve a single source of truth, so that quantity isn't asserted separately here
        return when (itemEnum) {
            STONE -> Item.Material.Stone(quantity!!)
            COAL -> Item.Material.Coal(quantity!!)
            PLUS_PICKAXE -> Item.Pickaxe.PlusPickaxe()
            CROSS_PICKAXE -> Item.Pickaxe.CrossPickaxe()
            SQUARE_PICKAXE -> Item.Pickaxe.SquarePickaxe()
        }
    }
}
