package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.Joins
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.model.BaseItem
import com.bitwiserain.pbbg.domain.model.InventoryItem
import com.bitwiserain.pbbg.domain.usecase.*
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class EquipmentUCImpl(private val db: Database) : EquipmentUC {
    override fun equip(userId: Int, itemId: Long): Unit = transaction(db) {
        val userId = EntityID(userId, UserTable)

        val inventoryItems = Joins.getInventoryItems(userId)

        /* Get item from inventory table */
        val item = inventoryItems[itemId] ?: throw InventoryItemNotFoundException(itemId)

        /* Make sure item is equippable */
        if (item !is InventoryItem.Equippable) throw InventoryItemNotEquippableException(itemId)
        /* Make sure item is not already equipped */
        if (item.equipped) throw InventoryItemAlreadyEquippedException(itemId)

        /* Get all equipped items */
        val equippedItems = inventoryItems.filter {
            val i = it.value
            i is InventoryItem.Equippable && i.equipped
        }

        /* Get category of equipment */
        val isSameCategoryAsTargetItem = fun(entry: Map.Entry<Long, InventoryItem>) = when (item.base) {
            is BaseItem.Pickaxe -> entry.value.base is BaseItem.Pickaxe
            else -> throw Exception("Item is not equippable and somehow made it through the check")
        }

        /* Unequip item currently in this equipment slot if any */
        equippedItems.filter(isSameCategoryAsTargetItem).entries.singleOrNull()?.let {
            Joins.setItemEquipped(userId, it.key, equipped = false)
        }

        /* Equip item */
        Joins.setItemEquipped(userId, itemId, equipped = true)
    }

    override fun unequip(userId: Int, itemId: Long): Unit = transaction(db) {
        val userId = EntityID(userId, UserTable)

        /* Get item from inventory table */
        val item = Joins.getInventoryItem(userId, itemId) ?: throw InventoryItemNotFoundException(itemId)

        /* Make sure item is equippable */
        if (item !is InventoryItem.Equippable) throw InventoryItemNotEquippableException(itemId)
        /* Make sure item is already unequipped */
        if (!item.equipped) throw InventoryItemNotEquippedException(itemId)

        /* Unequip item */
        Joins.setItemEquipped(userId, itemId, equipped = false)
    }
}
