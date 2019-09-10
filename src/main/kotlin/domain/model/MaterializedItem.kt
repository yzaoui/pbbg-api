package com.bitwiserain.pbbg.domain.model

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
        override fun copy(quantity: Int): Stackable = Stone(quantity)
    }
    class Coal(override val quantity: Int) : MaterializedItem(), Stackable {
        override val base get() = BaseItem.Material.Coal
        override fun copy(quantity: Int): Stackable = Coal(quantity)
    }
    class CopperOre(override val quantity: Int) : MaterializedItem(), Stackable {
        override val base get() = BaseItem.Material.CopperOre
        override fun copy(quantity: Int): Stackable = CopperOre(quantity)
    }

    object IcePick : MaterializedItem() {
        override val base get() = BaseItem.Pickaxe.IcePick
    }
    object PlusPickaxe : MaterializedItem() {
        override val base get() = BaseItem.Pickaxe.PlusPickaxe
    }
    object CrossPickaxe : MaterializedItem() {
        override val base get() = BaseItem.Pickaxe.CrossPickaxe
    }
    object SquarePickaxe : MaterializedItem() {
        override val base get() = BaseItem.Pickaxe.SquarePickaxe
    }
}
