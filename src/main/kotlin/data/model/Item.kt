package pbbg.data.model

import pbbg.data.model.ItemType.*

/**
 * Items that can go in the inventory.
 */
enum class Item(val type: ItemType, val friendlyName: String, val spriteName: String) {
    STONE(MATERIAL, "Stone", "stone"),
    COAL(MATERIAL, "Coal", "coal"),
    PLUS_PICKAXE(PICKAXE, "Plus-shaped Pickaxe", "plus_pickaxe"),
    CROSS_PICKAXE(PICKAXE, "Cross-shaped Pickaxe", "cross_pickaxe"),
    SQUARE_PICKAXE(PICKAXE, "Square-shaped Pickaxe", "square_pickaxe")
}

enum class ItemType {
    MATERIAL,
    PICKAXE
}
