package com.bitwiserain.pbbg.app.domain.usecase

interface EquipmentUC {
    /**
     * Equips a given item. If an item is already equipped in the slot this item belongs to, unequips it.
     *
     * @throws InventoryItemNotFoundException when this user-item combination is not found
     * @throws InventoryItemNotEquippableException when this item is not equippable
     * @throws InventoryItemAlreadyEquippedException when this item is already equipped
     */
    fun equip(userId: Int, itemId: Long)

    /**
     * Unequips a given item.
     *
     * @throws InventoryItemNotFoundException when this user-item combination is not found
     * @throws InventoryItemNotEquippableException when this item is not equippable
     * @throws InventoryItemNotEquippedException when this item is already unequipped
     */
    fun unequip(userId: Int, itemId: Long)
}

class InventoryItemNotFoundException(val itemId: Long) : Exception()
class InventoryItemNotEquippableException(val itemId: Long) : Exception()
class InventoryItemAlreadyEquippedException(val itemId: Long) : Exception()
class InventoryItemNotEquippedException(val itemId: Long) : Exception()
