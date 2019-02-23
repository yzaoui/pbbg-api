package com.bitwiserain.pbbg.domain.model

interface Stackable {
    val quantity: Int
}

interface Equippable {
    val equipped: Boolean
}

interface GridPreviewable {
    val grid: Set<Point>
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
            override val enum get() = ItemEnum.STONE
            override val friendlyName get() = "Stone"
            override val spriteName get() = "stone"
            override val description get() = "Some description about stone."
        }
        class Coal(override val quantity: Int) : Material() {
            override val enum get() = ItemEnum.COAL
            override val friendlyName get() = "Coal"
            override val spriteName get() = "coal"
            override val description get() = "Check this out, coal description."
        }
        class CopperOre(override val quantity: Int) : Material() {
            override val enum get() = ItemEnum.COPPER_ORE
            override val friendlyName get() = "Copper Ore"
            override val spriteName get() = "copper-ore"
            override val description get() = "I love orange!"
        }
    }

    sealed class Pickaxe : Item(), Equippable, GridPreviewable {
        class PlusPickaxe(override val equipped: Boolean) : Pickaxe() {
            override val enum get() = ItemEnum.PLUS_PICKAXE
            override val friendlyName get() = "Plus-shaped Pickaxe"
            override val spriteName get() = "plus-pickaxe"
            override val description get() = "Plus I can't even think about a description for this."
            override val grid get() = com.bitwiserain.pbbg.domain.model.mine.Pickaxe.PLUS.cells
        }
        class CrossPickaxe(override val equipped: Boolean) : Pickaxe() {
            override val enum get() = ItemEnum.CROSS_PICKAXE
            override val friendlyName get() = "Cross-shaped Pickaxe"
            override val spriteName get() = "cross-pickaxe"
            override val description get() = "\"You'd better not cross me when I'm holding this!\"."
            override val grid get() = com.bitwiserain.pbbg.domain.model.mine.Pickaxe.CROSS.cells
        }
        class SquarePickaxe(override val equipped: Boolean) : Pickaxe() {
            override val enum get() = ItemEnum.SQUARE_PICKAXE
            override val friendlyName get() = "Square-shaped Pickaxe"
            override val spriteName get() = "square-pickaxe"
            override val description get() = "Don't be a square."
            override val grid get() = com.bitwiserain.pbbg.domain.model.mine.Pickaxe.SQUARE.cells
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
