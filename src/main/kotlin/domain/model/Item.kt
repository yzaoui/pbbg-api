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
 */
sealed class Item {
    abstract val enum: ItemEnum

    sealed class Material : Item(), Stackable {
        class Stone(override val quantity: Int) : Material() {
            override val enum get() = ItemEnum.STONE
        }
        class Coal(override val quantity: Int) : Material() {
            override val enum get() = ItemEnum.COAL
        }
        class CopperOre(override val quantity: Int) : Material() {
            override val enum get() = ItemEnum.COPPER_ORE
        }
    }

    sealed class Pickaxe : Item(), Equippable, GridPreviewable {
        class PlusPickaxe(override val equipped: Boolean) : Pickaxe() {
            override val enum get() = ItemEnum.PLUS_PICKAXE
            override val grid get() = com.bitwiserain.pbbg.domain.model.mine.Pickaxe.PLUS.cells
        }
        class CrossPickaxe(override val equipped: Boolean) : Pickaxe() {
            override val enum get() = ItemEnum.CROSS_PICKAXE
            override val grid get() = com.bitwiserain.pbbg.domain.model.mine.Pickaxe.CROSS.cells
        }
        class SquarePickaxe(override val equipped: Boolean) : Pickaxe() {
            override val enum get() = ItemEnum.SQUARE_PICKAXE
            override val grid get() = com.bitwiserain.pbbg.domain.model.mine.Pickaxe.SQUARE.cells
        }
    }
}
