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
    val exp = this[UnitTable.exp]

    return when (unitEnum) {
        MyUnitEnum.ICE_CREAM_WIZARD -> MyUnit.IceCreamWizard(id, hp, maxHP, atk, def, exp)
        MyUnitEnum.TWOLIP -> MyUnit.Twolip(id, hp, maxHP, atk, def, exp)
        MyUnitEnum.CARPSHOOTER -> MyUnit.Carpshooter(id, hp, maxHP, atk, def, exp)
        MyUnitEnum.FLAMANGO -> MyUnit.Flamango(id, hp, maxHP, atk, def, exp)
    }
}

fun ResultRow.toMaterializedItem(): MaterializedItem {
    val itemEnum = this[MaterializedItemTable.itemEnum]
    val quantity = this[MaterializedItemTable.quantity]

    // TODO: Find a way to preserve a single source of truth, so that quantity isn't asserted separately here
    return when (itemEnum) {
        ItemEnum.STONE -> MaterializedItem.Stone(quantity!!)
        ItemEnum.COAL -> MaterializedItem.Coal(quantity!!)
        ItemEnum.COPPER_ORE -> MaterializedItem.CopperOre(quantity!!)
        ItemEnum.PLUS_PICKAXE -> MaterializedItem.PlusPickaxe
        ItemEnum.CROSS_PICKAXE -> MaterializedItem.CrossPickaxe
        ItemEnum.SQUARE_PICKAXE -> MaterializedItem.SquarePickaxe
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
