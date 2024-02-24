package com.bitwiserain.pbbg.app.domain.model

sealed class MaterializedItem {
    abstract val base: BaseItem
    val enum: ItemEnum
        get() = base.enum

    interface Stackable {
        val quantity: Int
        fun copy(quantity: Int = this.quantity): Stackable
    }

    class Stone(override val quantity: Int) : MaterializedItem(), Stackable {
        override val base get() = BaseItem.Material.Stone
        override fun copy(quantity: Int): Stone = Stone(quantity)
    }
    class Coal(override val quantity: Int) : MaterializedItem(), Stackable {
        override val base get() = BaseItem.Material.Coal
        override fun copy(quantity: Int): Coal = Coal(quantity)
    }
    class CopperOre(override val quantity: Int) : MaterializedItem(), Stackable {
        override val base get() = BaseItem.Material.CopperOre
        override fun copy(quantity: Int): CopperOre = CopperOre(quantity)
    }

    data object IcePick : MaterializedItem() {
        override val base get() = BaseItem.Pickaxe.IcePick
    }
    data object PlusPickaxe : MaterializedItem() {
        override val base get() = BaseItem.Pickaxe.PlusPickaxe
    }
    data object CrossPickaxe : MaterializedItem() {
        override val base get() = BaseItem.Pickaxe.CrossPickaxe
    }
    data object SquarePickaxe : MaterializedItem() {
        override val base get() = BaseItem.Pickaxe.SquarePickaxe
    }

    class AppleSapling(override val quantity: Int) : MaterializedItem(), Stackable {
        override val base get() = BaseItem.Sapling.AppleSapling
        override fun copy(quantity: Int): AppleSapling = AppleSapling(quantity)
    }

    class TomatoSeed(override val quantity: Int) : MaterializedItem(), Stackable {
        override val base get() = BaseItem.Seed.TomatoSeed
        override fun copy(quantity: Int): TomatoSeed = TomatoSeed(quantity)
    }

    class Apple(override val quantity: Int) : MaterializedItem(), Stackable {
        override val base get() = BaseItem.Apple
        override fun copy(quantity: Int): Apple = Apple(quantity)
    }

    class Tomato(override val quantity: Int) : MaterializedItem(), Stackable {
        override val base get() = BaseItem.Tomato
        override fun copy(quantity: Int): Tomato = Tomato(quantity)
    }
}
