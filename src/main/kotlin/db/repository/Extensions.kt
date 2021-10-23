package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.*
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.ResultSet

fun <T : Any> String.execAndMap(transform: (ResultSet) -> T): List<T> {
    val result = arrayListOf<T>()
    TransactionManager.current().exec(this) { rs ->
        while (rs.next()) {
            result += transform(rs)
        }
    }
    return result
}

fun ResultRow.toMyUnit(): MyUnit {
    val id = this[UnitTable.id].value
    val unitEnum = this[UnitTable.unit]
    val hp = this[UnitTable.hp]
    val maxHP = this[UnitTable.maxHP]
    val atk = this[UnitTable.atk]
    val def = this[UnitTable.def]
    val int = this[UnitTable.int]
    val res = this[UnitTable.res]
    val exp = this[UnitTable.exp]

    return when (unitEnum) {
        MyUnitEnum.ICE_CREAM_WIZARD -> MyUnit.IceCreamWizard(id, hp, maxHP, atk, def, int, res, exp)
        MyUnitEnum.TWOLIP -> MyUnit.Twolip(id, hp, maxHP, atk, def, int, res, exp)
        MyUnitEnum.CARPSHOOTER -> MyUnit.Carpshooter(id, hp, maxHP, atk, def, int, res, exp)
        MyUnitEnum.FLAMANGO -> MyUnit.Flamango(id, hp, maxHP, atk, def, int, res, exp)
    }
}

fun ResultRow.toMaterializedItem(): MaterializedItem {
    val itemEnum = this[MaterializedItemTableImpl.Exposed.itemEnum]
    val quantity = this[MaterializedItemTableImpl.Exposed.quantity]

    // TODO: Find a way to preserve a single source of truth, so that quantity isn't asserted separately here
    return when (itemEnum) {
        ItemEnum.STONE -> MaterializedItem.Stone(quantity!!)
        ItemEnum.COAL -> MaterializedItem.Coal(quantity!!)
        ItemEnum.COPPER_ORE -> MaterializedItem.CopperOre(quantity!!)
        ItemEnum.ICE_PICK -> MaterializedItem.IcePick
        ItemEnum.PLUS_PICKAXE -> MaterializedItem.PlusPickaxe
        ItemEnum.CROSS_PICKAXE -> MaterializedItem.CrossPickaxe
        ItemEnum.SQUARE_PICKAXE -> MaterializedItem.SquarePickaxe
        ItemEnum.APPLE_SAPLING -> MaterializedItem.AppleSapling(quantity!!)
        ItemEnum.TOMATO_SEED -> MaterializedItem.TomatoSeed(quantity!!)
        ItemEnum.APPLE -> MaterializedItem.Apple(quantity!!)
        ItemEnum.TOMATO -> MaterializedItem.Tomato(quantity!!)
    }
}

fun ResultRow.toInventoryItem(): InventoryItem {
    val materializedItem = toMaterializedItem()

    return if (materializedItem.base is BaseItem.Equippable) {
        val equipped = this[InventoryTable.equipped]!!

        InventoryItem.EquippableInventoryItem(materializedItem, equipped)
    } else {
        InventoryItem(materializedItem)
    }
}
