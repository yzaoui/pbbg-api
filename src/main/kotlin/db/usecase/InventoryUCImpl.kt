package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.InventoryTable
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.model.Inventory
import com.bitwiserain.pbbg.domain.model.InventoryEntry
import com.bitwiserain.pbbg.domain.model.Item
import com.bitwiserain.pbbg.domain.usecase.InventoryUC
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class InventoryUCImpl(private val db: Database) : InventoryUC {
    override fun getInventory(userId: Int): Inventory = transaction(db) {
        // TODO: Consider checking if user exists
        val items = InventoryTable.select { InventoryTable.userId.eq(userId) }
            .map { it.toInventoryItem() }

        Inventory(items)
    }

    override fun storeInInventory(userId: Int, item: Item, quantity: Int): Unit = transaction(db) {
        // TODO: Consider checking if user exists
        val entry = InventoryTable.select { InventoryTable.userId.eq(userId) and InventoryTable.item.eq(item) }
            .map { it.toInventoryItem() }
            .singleOrNull()

        if (entry != null) {
            InventoryTable.update({ InventoryTable.userId.eq(userId) and InventoryTable.item.eq(item) }) {
                with (SqlExpressionBuilder) {
                    it.update(InventoryTable.quantity, InventoryTable.quantity + quantity)
                }
            }
        } else {
            InventoryTable.insert {
                it[InventoryTable.userId] = EntityID(userId, UserTable)
                it[InventoryTable.item] = item
                it[InventoryTable.quantity] = quantity
            }
        }
    }

    private fun ResultRow.toInventoryItem() = InventoryEntry(
        item = this[InventoryTable.item],
        quantity = this[InventoryTable.quantity]
    )
}
