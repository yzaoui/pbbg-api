package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.*
import com.bitwiserain.pbbg.domain.model.Inventory
import com.bitwiserain.pbbg.domain.model.MaterializedItem
import com.bitwiserain.pbbg.domain.model.MaterializedItem.Stackable
import com.bitwiserain.pbbg.domain.usecase.InventoryUC
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class InventoryUCImpl(private val db: Database) : InventoryUC {
    override fun getInventory(userId: Int): Inventory = transaction(db) {
        // TODO: Consider checking if user exists
        val userId = EntityID(userId, UserTable)

        val items = Joins.getInventoryItems(userId)

        Inventory(items)
    }

    override fun storeInInventory(userId: Int, itemToStore: MaterializedItem): Unit = transaction(db) {
        // TODO: Consider checking if user exists
        val userId = EntityID(userId, UserTable)

        val heldItems = Joins.getHeldItemsOfBaseKind(userId, itemToStore.enum)

        if (heldItems.count() == 1 && heldItems.values.single() is Stackable) {
            itemToStore as Stackable
            // If this kind of item is already stored, and it can be stacked, increase its quantity
            MaterializedItemTable.updateQuantity(heldItems.keys.single(), itemToStore.quantity)
        } else {
            // If this item isn't already stored, or it can't be stacked, create a new row for it
            val itemId = MaterializedItemTable.insertItemAndGetId(itemToStore)
            InventoryTable.insertItem(userId, itemId, itemToStore.base)
        }

        /*************************************************
         * Update user's dex to check off this item type *
         *************************************************/
        if (!DexTable.hasEntry(userId, itemToStore.enum)) DexTable.insertDiscovered(userId, itemToStore.enum)
    }
}
