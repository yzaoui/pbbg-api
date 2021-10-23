package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.*
import com.bitwiserain.pbbg.domain.model.MaterializedItem
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistory
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistoryInfo
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

fun storeInInventoryReturnItemID(
    db: Database,
    now: Instant,
    userId: Int,
    itemToStore: MaterializedItem,
    historyInfo: ItemHistoryInfo,
    dexTable: DexTable,
    itemHistoryTable: ItemHistoryTable,
    materializedItemTable: MaterializedItemTable,
): Long = transaction(db) {
    val heldItems = Joins.getHeldItemsOfBaseKind(userId, itemToStore.enum)

    val itemId: Long

    if (heldItems.count() == 1 && heldItems.values.single() is MaterializedItem.Stackable) {
        itemToStore as MaterializedItem.Stackable
        // If this item is currently held and Stackable, increase its quantity
        itemId = heldItems.keys.single()
        materializedItemTable.updateQuantity(heldItems.keys.single(), itemToStore.quantity)
    } else {
        // If this item isn't already stored, or it can't be stacked, create a new entry for it
        itemId = materializedItemTable.insertItemAndGetId(itemToStore)
        InventoryTable.insertItem(userId, itemId, itemToStore.base)

        if (heldItems.count() == 0) {
            // TODO: For now, assume only stackable items are MineEntity
            itemHistoryTable.insertItemHistory(itemId, ItemHistory(
                date = now,
                info = historyInfo
            ))
        }
    }

    /*************************************************
     * Update user's dex to check off this item type *
     *************************************************/
    if (!dexTable.hasEntry(userId, itemToStore.enum)) dexTable.insertDiscovered(userId, itemToStore.enum)

    itemId
}
