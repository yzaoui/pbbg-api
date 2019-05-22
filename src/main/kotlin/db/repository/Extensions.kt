package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.MyUnit
import com.bitwiserain.pbbg.domain.model.MyUnitEnum
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
    val exp = this[UnitTable.exp]

    return when (unitEnum) {
        MyUnitEnum.ICE_CREAM_WIZARD -> MyUnit.IceCreamWizard(id, hp, maxHP, atk, exp)
        MyUnitEnum.TWOLIP -> MyUnit.Twolip(id, hp, maxHP, atk, exp)
        MyUnitEnum.CARPSHOOTER -> MyUnit.Carpshooter(id, hp, maxHP, atk, exp)
        MyUnitEnum.FLAMANGO -> MyUnit.Flamango(id, hp, maxHP, atk, exp)
    }
}
