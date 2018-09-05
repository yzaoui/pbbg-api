package com.bitwiserain.pbbg.domain.model

import com.bitwiserain.pbbg.domain.model.ItemCategory.*

/**
 * Items that can go in the inventory.
 */
enum class Item(val category: ItemCategory, val friendlyName: String, val spriteName: String) {
    STONE(MATERIAL, "Stone", "stone"),
    COAL(MATERIAL, "Coal", "coal"),
    PLUS_PICKAXE(PICKAXE, "Plus-shaped Pickaxe", "plus-pickaxe"),
    CROSS_PICKAXE(PICKAXE, "Cross-shaped Pickaxe", "cross-pickaxe"),
    SQUARE_PICKAXE(PICKAXE, "Square-shaped Pickaxe", "square-pickaxe")
}
