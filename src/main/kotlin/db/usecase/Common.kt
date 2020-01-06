package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.*
import com.bitwiserain.pbbg.domain.model.MaterializedItem
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistory
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistoryInfo
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

fun storeInInventoryReturnItemID(db: Database, now: Instant, userId: EntityID<Int>, itemToStore: MaterializedItem, historyInfo: ItemHistoryInfo): EntityID<Long> = transaction(db) {
    val heldItems = Joins.getHeldItemsOfBaseKind(userId, itemToStore.enum)

    val itemId: EntityID<Long>

    if (heldItems.count() == 1 && heldItems.values.single() is MaterializedItem.Stackable) {
        itemToStore as MaterializedItem.Stackable
        // If this item is currently held and Stackable, increase its quantity
        itemId = heldItems.keys.single()
        MaterializedItemTable.updateQuantity(heldItems.keys.single(), itemToStore.quantity)
    } else {
        // If this item isn't already stored, or it can't be stacked, create a new entry for it
        itemId = MaterializedItemTable.insertItemAndGetId(itemToStore)
        InventoryTable.insertItem(userId, itemId, itemToStore.base)

        if (heldItems.count() == 0) {
            // TODO: For now, assume only stackable items are MineEntity
            ItemHistoryTable.insertItemHistory(itemId.value, ItemHistory(
                date = now,
                info = historyInfo
            ))
        }
    }

    /*************************************************
     * Update user's dex to check off this item type *
     *************************************************/
    if (!DexTable.hasEntry(userId, itemToStore.enum)) DexTable.insertDiscovered(userId, itemToStore.enum)

    itemId
}