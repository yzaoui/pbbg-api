package com.bitwiserain.pbbg.domain.model

open class InventoryItem(
    val item: MaterializedItem
) {
    interface Equippable {
        val equipped: Boolean
    }

    class EquippableInventoryItem(item: MaterializedItem, override val equipped: Boolean) : InventoryItem(item), Equippable

    val base: BaseItem
        get() = item.base
}
