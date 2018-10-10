package com.bitwiserain.pbbg.domain.model

interface Stackable {
    val quantity: Int
}

interface Equippable {
    val equipped: Boolean
}

/**
 * Item that can go in the inventory.
 *
 * @property enum The enum counterpart to this item.
 * @property friendlyName Human-friendly name to be displayed to the user.
 * @property spriteName The base sprite name which can be used to retrieve the corresponding image file at a desired resolution.
 * @property description The description of this item.
 */
sealed class Item {
    abstract val enum: ItemEnum
    abstract val friendlyName: String
    abstract val spriteName: String
    abstract val description: String

    sealed class Material : Item(), Stackable {
        class Stone(override val quantity: Int) : Material() {
            override val enum = ItemEnum.STONE
            override val friendlyName = "Stone"
            override val spriteName = "stone"
            override val description = "Some description about stone."
        }
        class Coal(override val quantity: Int) : Material() {
            override val enum = ItemEnum.COAL
            override val friendlyName = "Coal"
            override val spriteName = "coal"
            override val description = "Check this out, coal description."
        }
        class CopperOre(override val quantity: Int) : Material() {
            override val enum = ItemEnum.COPPER_ORE
            override val friendlyName = "Copper Ore"
            override val spriteName = "copper-ore"
            override val description = "I love orange!"
        }
    }

    sealed class Pickaxe : Item(), Equippable {
        class PlusPickaxe(override val equipped: Boolean) : Pickaxe() {
            override val enum = ItemEnum.PLUS_PICKAXE
            override val friendlyName = "Plus-shaped Pickaxe"
            override val spriteName = "plus-pickaxe"
            override val description = "Plus I can't even think about a description for this."
        }
        class CrossPickaxe(override val equipped: Boolean) : Pickaxe() {
            override val enum = ItemEnum.CROSS_PICKAXE
            override val friendlyName = "Cross-shaped Pickaxe"
            override val spriteName = "cross-pickaxe"
            override val description = "\"You'd better not cross me when I'm holding this!\"."
        }
        class SquarePickaxe(override val equipped: Boolean) : Pickaxe() {
            override val enum = ItemEnum.SQUARE_PICKAXE
            override val friendlyName = "Square-shaped Pickaxe"
            override val spriteName = "square-pickaxe"
            override val description = "Don't be a square."
        }
    }
}

enum class ItemEnum {
    STONE,
    COAL,
    COPPER_ORE,
    PLUS_PICKAXE,
    CROSS_PICKAXE,
    SQUARE_PICKAXE
}
