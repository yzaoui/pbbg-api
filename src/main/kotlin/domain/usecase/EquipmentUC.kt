package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.Item
import com.bitwiserain.pbbg.domain.model.Pickaxe

interface EquipmentUC {
    /**
     * @throws InventoryItemNotFoundException when this user-item combination is not found
     * @throws InventoryItemNotEquippable when this item is not equippable
     */
    fun equip(userId: Int, inventoryItemId: Int)

    /**
     * @throws InventoryItemNotFoundException when this user-item combination is not found
     * @throws InventoryItemNotEquippable when this item is not equippable
     */
    fun unequip(userId: Int, inventoryItemId: Int)

    fun getEquippedPickaxe(userId: Int): Pickaxe?
    fun generatePickaxe(userId: Int): Item.Pickaxe? // TODO: Temporary use case until proper way to obtain pickaxe is implemented
}

class InventoryItemNotFoundException(val itemId: Int) : Exception()
class InventoryItemNotEquippable(val itemId: Int) : Exception()
