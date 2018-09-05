package com.bitwiserain.pbbg.domain.model

import com.bitwiserain.pbbg.domain.model.ItemCategory.*

/**
 * Item that can go in the inventory.
 *
 * @property category The category this item fits in for common behavior. TODO: Unused
 * @property friendlyName Human-friendly name to be displayed to the user.
 * @property spriteName The base sprite name which can be used to retrieve the corresponding image file at a desired resolution.
 */
enum class Item(val category: ItemCategory, val friendlyName: String, val spriteName: String) {
    STONE(MATERIAL, "Stone", "stone"),
    COAL(MATERIAL, "Coal", "coal"),
    PLUS_PICKAXE(PICKAXE, "Plus-shaped Pickaxe", "plus-pickaxe"),
    CROSS_PICKAXE(PICKAXE, "Cross-shaped Pickaxe", "cross-pickaxe"),
    SQUARE_PICKAXE(PICKAXE, "Square-shaped Pickaxe", "square-pickaxe")
}
