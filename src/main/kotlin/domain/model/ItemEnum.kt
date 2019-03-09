package com.bitwiserain.pbbg.domain.model

/**
 * @param friendlyName Human-friendly name to be displayed to the user.
 * @param spriteName The base sprite name which can be used to retrieve the corresponding image file at a desired resolution.
 * @param description The description of this item.
 */
enum class ItemEnum(val friendlyName: String, val spriteName: String, val description: String) {
    STONE("Stone", "stone", "Some description about stone."),
    COAL("Coal", "coal", "Check this out, coal description."),
    COPPER_ORE("Copper Ore", "copper-ore", "I love orange!"),
    PLUS_PICKAXE("Plus-shaped Pickaxe", "plus-pickaxe", "Plus I can't even think about a description for this."),
    CROSS_PICKAXE("Cross-shaped Pickaxe", "cross-pickaxe", "\"You'd better not cross me when I'm holding this!\"."),
    SQUARE_PICKAXE("Square-shaped Pickaxe", "square-pickaxe", "Don't be a square.")
}
