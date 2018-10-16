package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.Pickaxe

interface EquipmentUC {
    /**
     * @throws InventoryItemNotFoundException when this user-item combination is not found
     * @throws InventoryItemNotEquippable when this item is not equippable
     * @throws InventoryItemAlreadyEquipped when this item is already equipped
     */
    fun equip(userId: Int, inventoryItemId: Int)

    /**
     * @throws InventoryItemNotFoundException when this user-item combination is not found
     * @throws InventoryItemNotEquippable when this item is not equippable
     * @throws InventoryItemAlreadyUnequipped when this item is already unequipped
     */
    fun unequip(userId: Int, inventoryItemId: Int)

    fun getEquippedPickaxe(userId: Int): Pickaxe?
}

class InventoryItemNotFoundException(val itemId: Int) : Exception()
class InventoryItemNotEquippable(val itemId: Int) : Exception()
class InventoryItemAlreadyEquipped(val itemId: Int) : Exception()
class InventoryItemAlreadyUnequipped(val itemId: Int) : Exception()
