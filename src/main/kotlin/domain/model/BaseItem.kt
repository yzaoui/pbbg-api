package com.bitwiserain.pbbg.domain.model

import com.bitwiserain.pbbg.domain.model.farm.BasePlant

sealed class BaseItem {
    interface Stackable
    interface Equippable
    interface GridPreviewable {
        val grid: Set<Point>
    }
    interface Plantable {
        val basePlant: BasePlant
    }

    abstract val enum: ItemEnum
    abstract val friendlyName: String
    abstract val spriteName: String
    abstract val description: String

    sealed class Material : BaseItem(), Stackable {
        object Stone : Material() {
            override val enum: ItemEnum get() = ItemEnum.STONE
            override val friendlyName = "Stone"
            override val spriteName = "stone"
            override val description = "Some description about stone."
        }
        object Coal : Material() {
            override val enum: ItemEnum get() = ItemEnum.COAL
            override val friendlyName = "Coal"
            override val spriteName = "coal"
            override val description = "Check this out, coal description."
        }
        object CopperOre : Material() {
            override val enum: ItemEnum get() = ItemEnum.COPPER_ORE
            override val friendlyName = "Copper Ore"
            override val spriteName = "copper-ore"
            override val description = "I love orange!"
        }
    }

    sealed class Pickaxe : BaseItem(), Equippable, GridPreviewable {
        object IcePick : Pickaxe() {
            override val enum: ItemEnum get() = ItemEnum.ICE_PICK
            override val friendlyName = "Ice Pick"
            override val spriteName = "ice-pick"
            override val description = "Not quite a pickaxe, but good enough."
            override val grid get() = arrayOf(
                arrayOf(0, 0, 0),
                arrayOf(0, 1, 0),
                arrayOf(0, 0, 0)
            ).toPoints(3, 3, Point(1, 1))
        }
        object PlusPickaxe : Pickaxe() {
            override val enum: ItemEnum get() = ItemEnum.PLUS_PICKAXE
            override val friendlyName = "Plus-shaped Pickaxe"
            override val spriteName = "plus-pickaxe"
            override val description = "With this in hand, watch your materials add up."
            override val grid get() = arrayOf(
                arrayOf(0, 1, 0),
                arrayOf(1, 1, 1),
                arrayOf(0, 1, 0)
            ).toPoints(3, 3, Point(1, 1))
        }
        object CrossPickaxe : Pickaxe() {
            override val enum: ItemEnum get() = ItemEnum.CROSS_PICKAXE
            override val friendlyName = "Cross-shaped Pickaxe"
            override val spriteName = "cross-pickaxe"
            override val description = "Some description about cross-shaped pickaxe."
            override val grid get() = arrayOf(
                arrayOf(1, 0, 1),
                arrayOf(0, 1, 0),
                arrayOf(1, 0, 1)
            ).toPoints(3, 3, Point(1, 1))
        }
        object SquarePickaxe : Pickaxe() {
            override val enum: ItemEnum get() = ItemEnum.SQUARE_PICKAXE
            override val friendlyName = "Square-shaped Pickaxe"
            override val spriteName = "square-pickaxe"
            override val description = "Conveniently-shaped pickaxe for squaring up."
            override val grid get() = arrayOf(
                arrayOf(1, 1, 1),
                arrayOf(1, 0, 1),
                arrayOf(1, 1, 1)
            ).toPoints(3, 3, Point(1, 1))
        }
    }

    sealed class Sapling : BaseItem(), Stackable, Plantable {
        object AppleSapling : Sapling() {
            override val enum: ItemEnum get() = ItemEnum.APPLE_SAPLING
            override val friendlyName: String get() = "Apple Sapling"
            override val spriteName: String get() = "apple-sapling"
            override val description: String get() = "Apple sapling description here."
            override val basePlant: BasePlant get() = BasePlant.AppleTree
        }
    }

    sealed class Seed : BaseItem(), Stackable, Plantable {
        object TomatoSeed : Seed() {
            override val enum: ItemEnum get() = ItemEnum.TOMATO_SEED
            override val friendlyName: String get() = "Tomato Seed"
            override val spriteName: String get() = "tomato-seed"
            override val description: String get() = "Tomato seed description here."
            override val basePlant: BasePlant get() = BasePlant.TomatoPlant
        }
    }

    object Apple : BaseItem(), Stackable {
        override val enum: ItemEnum get() = ItemEnum.APPLE
        override val friendlyName: String get() = "Apple"
        override val spriteName: String get() = "apple"
        override val description: String get() = "Apple description here."
    }
}

private fun Array<Array<Int>>.toPoints(width: Int, height: Int, center: Point): Set<Point> {
    val set = mutableSetOf<Point>()

    for (row in 0 until height) {
        for (col in 0 until width) {
            if (this[row][col] == 1) set.add(Point(col - center.x, row - center.y))
        }
    }

    return set
}
