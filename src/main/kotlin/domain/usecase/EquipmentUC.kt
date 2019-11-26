package com.bitwiserain.pbbg.domain.usecase

interface EquipmentUC {
    /**
     * Equips a given item. If an item is already equipped in the slot this item belongs to, unequips it.
     *
     * @throws InventoryItemNotFoundException when this user-item combination is not found
     * @throws InventoryItemNotEquippable when this item is not equippable
     * @throws InventoryItemAlreadyEquipped when this item is already equipped
     */
    fun equip(userId: Int, itemId: Long)

    /**
     * Unequips a given item.
     *
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
