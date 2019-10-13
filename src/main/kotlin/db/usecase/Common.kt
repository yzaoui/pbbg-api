package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.DexTable
import com.bitwiserain.pbbg.db.repository.InventoryTable
import com.bitwiserain.pbbg.db.repository.Joins
import com.bitwiserain.pbbg.db.repository.MaterializedItemTable
import com.bitwiserain.pbbg.domain.model.MaterializedItem
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

fun storeInInventoryReturnItemID(db: Database, userId: EntityID<Int>, itemToStore: MaterializedItem): EntityID<Long> = transaction(db) {
    val heldItems = Joins.getHeldItemsOfBaseKind(userId, itemToStore.enum)

    val itemId: EntityID<Long>

    if (heldItems.count() == 1 && heldItems.values.single() is MaterializedItem.Stackable) {
        itemToStore as MaterializedItem.Stackable
        // If this kind of item is already stored, and it can be stacked, increase its quantity
        itemId = heldItems.keys.single()
        MaterializedItemTable.updateQuantity(heldItems.keys.single(), itemToStore.quantity)
    } else {
        // If this item isn't already stored, or it can't be stacked, create a new row for it
        itemId = MaterializedItemTable.insertItemAndGetId(itemToStore)
        InventoryTable.insertItem(userId, itemId, itemToStore.base)
    }

    /*************************************************
     * Update user's dex to check off this item type *
     *************************************************/

    /*************************************************
     * Update user's dex to check off this item type *
     *************************************************/
    if (!DexTable.hasEntry(userId, itemToStore.enum)) DexTable.insertDiscovered(userId, itemToStore.enum)

    itemId
}
