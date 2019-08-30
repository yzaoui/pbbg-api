package com.bitwiserain.pbbg.domain.model

sealed class MaterializedItem {
    abstract val base: BaseItem
    val enum: ItemEnum
        get() = base.enum

    interface Stackable {
        val quantity: Int
    }

    class Stone(override val quantity: Int) : MaterializedItem(), Stackable {
        override val base get() = BaseItem.Material.Stone
    }
    class Coal(override val quantity: Int) : MaterializedItem(), Stackable {
        override val base get() = BaseItem.Material.Coal
    }
    class CopperOre(override val quantity: Int) : MaterializedItem(), Stackable {
        override val base get() = BaseItem.Material.CopperOre
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
