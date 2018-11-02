package com.bitwiserain.pbbg

import com.bitwiserain.pbbg.db.form.MyUnitForm
import com.bitwiserain.pbbg.db.repository.SquadTable
import com.bitwiserain.pbbg.db.repository.UnitTable
import com.bitwiserain.pbbg.domain.model.MyUnit
import com.bitwiserain.pbbg.domain.model.MyUnit.*
import com.bitwiserain.pbbg.domain.model.MyUnitEnum.*
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import kotlin.math.max

fun addUnitToSquad(user: EntityID<Int>, unit: MyUnitForm) {
    val unitId = UnitTable.insertUnitAndGetId(unit)

    SquadTable.insert {
        it[SquadTable.user] = user
        it[SquadTable.unit] = unitId
    }
}

fun ResultRow.toCharUnit(): MyUnit {
    val id = this[UnitTable.id].value
    val unitEnum = this[UnitTable.unit]
    val hp = this[UnitTable.hp]
    val maxHP = this[UnitTable.maxHP]
    val atk = this[UnitTable.atk]
    val exp = this[UnitTable.exp]

    return when (unitEnum) {
        ICE_CREAM_WIZARD -> IceCreamWizard(id, hp, maxHP, atk, exp)
        TWOLIP -> Twolip(id, hp, maxHP, atk, exp)
        CARPSHOOTER -> Carpshooter(id, hp, maxHP, atk, exp)
    }
}

fun SquadTable.getAllies(userId: Int): List<MyUnit> {
    return innerJoin(UnitTable)
        .slice(UnitTable.columns)
        .select { SquadTable.user.eq(userId) }
        .map { it.toCharUnit() }
}

fun SquadTable.getAlly(userId: Int, allyId: Long): MyUnit? {
    return innerJoin(UnitTable)
        .slice(UnitTable.columns)
        .select { SquadTable.user.eq(userId) and UnitTable.id.eq(allyId) }
        .singleOrNull()
        ?.toCharUnit()
}

fun UnitTable.insertUnitAndGetId(unit: MyUnitForm): EntityID<Long> {
    return insertAndGetId {
        it[UnitTable.unit] = unit.enum
        it[UnitTable.hp] = unit.hp
        it[UnitTable.maxHP] = unit.hp
        it[UnitTable.atk] = unit.atk
        it[UnitTable.exp] = 0L
    }
}

fun UnitTable.updateUnit(unitId: Long, unit: MyUnit) {
    update({ UnitTable.id.eq(unitId) }) {
        it[UnitTable.hp] = unit.hp
        it[UnitTable.maxHP] = unit.maxHP
        it[UnitTable.atk] = unit.atk
        it[UnitTable.exp] = unit.exp
    }
}

fun MyUnit.receiveDamage(damage: Int): MyUnit {
    val newHp = max(hp - damage, 0)

    return when (this) {
        is IceCreamWizard -> copy(hp = newHp)
        is Twolip -> copy(hp = newHp)
        is Carpshooter -> copy(hp = newHp)
    }
}

fun MyUnit.gainExperience(gainedExp: Long): MyUnit {
    val newExp = exp + gainedExp

    return when (this) {
        is IceCreamWizard -> copy(exp = newExp)
        is Twolip -> copy(exp = newExp)
        is Carpshooter -> copy(exp = newExp)
    }
}
