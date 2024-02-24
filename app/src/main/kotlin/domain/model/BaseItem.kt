package com.bitwiserain.pbbg.app.domain.model

import com.bitwiserain.pbbg.app.domain.model.farm.BasePlant

sealed class BaseItem {
    interface Stackable
    interface Equippable
    interface GridPreviewable {
        val grid: Set<Point>
    }
    interface Plantable {
        val basePlant: BasePlant
    }

    val id: Int get() = enum.ordinal + 1
    abstract val enum: ItemEnum
    abstract val friendlyName: String
    abstract val spriteName: String
    abstract val description: String

    sealed class Material : BaseItem(), Stackable {
        object Stone : Material() {
            override val enum: ItemEnum get() = ItemEnum.STONE
            override val friendlyName = "Stone"
            override val spriteName = "stone"
            override val description = "Basic mining material. You don't seem to be able to determine its constituents."
        }
        object Coal : Material() {
            override val enum: ItemEnum get() = ItemEnum.COAL
            override val friendlyName = "Coal"
            override val spriteName = "coal"
            override val description = "Sedimentary rock. It can somehow found at the surface level of the mines."
        }
        object CopperOre : Material() {
            override val enum: ItemEnum get() = ItemEnum.COPPER_ORE
            override val friendlyName = "Copper Ore"
            override val spriteName = "copper-ore"
            override val description = "Ore containing copper metal. Wait too long and it'll turn green."
        }
    }

    sealed class Pickaxe : BaseItem(), Equippable, GridPreviewable {
        object IcePick : Pickaxe() {
            override val enum: ItemEnum get() = ItemEnum.ICE_PICK
            override val friendlyName = "Ice Pick"
            override val spriteName = "ice-pick"
            override val description = "Tool typically used to pick at ice. Not quite a pickaxe, but good enough."
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
            override val description = "Pickaxe with a plus-shaped head. With this in hand, you can watch your materials add up."
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
            override val description = "Pickaxe with a cross-shaped head. It looks pretty sharp, try to not ruin what you mine."
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
            override val description = "Pickaxe with a square-shaped head. The physics of mining with this don't seem quite right."
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
            override val description: String get() = "Sapling that will produce apples. It fills you with a vague sense of summer time longing."
            override val basePlant: BasePlant get() = BasePlant.AppleTree
        }
    }

    sealed class Seed : BaseItem(), Stackable, Plantable {
        object TomatoSeed : Seed() {
            override val enum: ItemEnum get() = ItemEnum.TOMATO_SEED
            override val friendlyName: String get() = "Tomato Seed"
            override val spriteName: String get() = "tomato-seed"
            override val description: String get() = "Seed that will produce tomatoes. It almost looks edible on its own."
            override val basePlant: BasePlant get() = BasePlant.TomatoPlant
        }
    }

    object Apple : BaseItem(), Stackable {
        override val enum: ItemEnum get() = ItemEnum.APPLE
        override val friendlyName: String get() = "Apple"
        override val spriteName: String get() = "apple"
        override val description: String get() = "Shiny red fruit. It is very versatile, if you watch out for the amygdalin."
    }

    object Tomato : BaseItem(), Stackable {
        override val enum: ItemEnum get() = ItemEnum.TOMATO
        override val friendlyName: String get() = "Tomato"
        override val spriteName: String get() = "tomato"
        override val description: String get() = "Crop which consists of 95% goo and 5% plant matter."
    }
}

private fun Array<Array<Int>>.toPoints(width: Int, height: Int, center: Point): Set<Point> {
    val set = mutableSetOf<Point>()

    for (row in 0..<height) {
        for (col in 0..<width) {
            if (this[row][col] == 1) set.add(Point(col - center.x, row - center.y))
        }
    }

    return set
}
