package com.bitwiserain.pbbg.domain.usecase

interface EquipmentUC {
    /**
     * @throws InventoryItemNotFoundException when this user-item combination is not found
     * @throws InventoryItemNotEquippable when this item is not equippable
     * @throws InventoryItemAlreadyEquipped when this item is already equipped
     */
    fun equip(userId: Int, itemId: Long)

    /**
     * @throws InventoryItemNotFoundException when this user-item combination is not found
     * @throws InventoryItemNotEquippable when this item is not equippable
     * @throws InventoryItemNotEquipped when this item is already unequipped
     */
    fun unequip(userId: Int, itemId: Long)
}

class InventoryItemNotFoundException(val itemId: Long) : Exception()
class InventoryItemNotEquippable(val itemId: Long) : Exception()
class InventoryItemAlreadyEquipped(val itemId: Long) : Exception()
class InventoryItemNotEquipped(val itemId: Long) : Exception()
