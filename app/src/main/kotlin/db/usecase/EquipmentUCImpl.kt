package com.bitwiserain.pbbg.app.db.usecase

import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.InventoryTable
import com.bitwiserain.pbbg.app.domain.model.BaseItem
import com.bitwiserain.pbbg.app.domain.model.InventoryItem
import com.bitwiserain.pbbg.app.domain.usecase.EquipmentUC
import com.bitwiserain.pbbg.app.domain.usecase.InventoryItemAlreadyEquippedException
import com.bitwiserain.pbbg.app.domain.usecase.InventoryItemNotEquippableException
import com.bitwiserain.pbbg.app.domain.usecase.InventoryItemNotEquippedException
import com.bitwiserain.pbbg.app.domain.usecase.InventoryItemNotFoundException

class EquipmentUCImpl(private val transaction: Transaction, private val inventoryTable: InventoryTable) : EquipmentUC {
    override fun equip(userId: Int, itemId: Long): Unit = transaction {
        val inventoryItems = inventoryTable.getInventoryItems(userId)

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
            inventoryTable.setItemEquipped(userId, it.key, equipped = false)
        }

        /* Equip item */
        inventoryTable.setItemEquipped(userId, itemId, equipped = true)
    }

    override fun unequip(userId: Int, itemId: Long): Unit = transaction {
        /* Get item from inventory table */
        val item = inventoryTable.getInventoryItem(userId, itemId) ?: throw InventoryItemNotFoundException(itemId)

        /* Make sure item is equippable */
        if (item !is InventoryItem.Equippable) throw InventoryItemNotEquippableException(itemId)
        /* Make sure item is already unequipped */
        if (!item.equipped) throw InventoryItemNotEquippedException(itemId)

        /* Unequip item */
        inventoryTable.setItemEquipped(userId, itemId, equipped = false)
    }
}
