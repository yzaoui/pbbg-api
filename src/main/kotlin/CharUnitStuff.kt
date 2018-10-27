package com.bitwiserain.pbbg

import com.bitwiserain.pbbg.CharUnit.*
import com.bitwiserain.pbbg.CharUnitEnum.*
import com.bitwiserain.pbbg.db.repository.UserTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

sealed class CharUnit {
    abstract val enum: CharUnitEnum
    abstract val hp: Int
    abstract val maxHP: Int
    abstract val atk: Int
    abstract val def: Int

    class IceCreamWizard(override val hp: Int, override val maxHP: Int, override val atk: Int, override val def: Int) : CharUnit() {
        override val enum get() = ICE_CREAM_WIZARD
    }

    class Twolip(override val hp: Int, override val maxHP: Int, override val atk: Int, override val def: Int) : CharUnit() {
        override val enum get() = TWOLIP
    }

    class Carpshooter(override val hp: Int, override val maxHP: Int, override val atk: Int, override val def: Int) : CharUnit() {
        override val enum get() = CARPSHOOTER
    }
}

enum class CharUnitEnum {
    ICE_CREAM_WIZARD,
    TWOLIP,
    CARPSHOOTER
}

object SquadTable : LongIdTable() {
    val user = reference("user_id", UserTable)
    val unit = reference("unit_id", UnitTable)
}

object UnitTable : LongIdTable() {
    val unit = enumeration("unit", CharUnitEnum::class)
    val hp = integer("hp")
    val maxHP = integer("max_hp")
    val atk = integer("atk")
    val def = integer("def")
}

interface UnitUC {
    fun getSquad(userId: Int): Squad
}

class UnitUCImpl(private val db: Database) : UnitUC {
    override fun getSquad(userId: Int): Squad = transaction(db) {
        val allies = SquadTable.getAllies(userId)

        Squad(allies)
    }
}

class Squad(val units: List<CharUnit>)

fun addUnitToSquad(user: EntityID<Int>, unit: CharUnit) {
    val unitId = UnitTable.insertUnitAndGetId(unit)

    SquadTable.insert {
        it[SquadTable.user] = user
        it[SquadTable.unit] = unitId
    }
}

fun ResultRow.toCharUnit(): CharUnit {
    val unitEnum = this[UnitTable.unit]
    val hp = this[UnitTable.hp]
    val maxHP = this[UnitTable.maxHP]
    val atk = this[UnitTable.atk]
    val def = this[UnitTable.def]

    return when (unitEnum) {
        ICE_CREAM_WIZARD -> IceCreamWizard(hp, maxHP, atk, def)
        TWOLIP -> Twolip(hp, maxHP, atk, def)
        CARPSHOOTER -> Carpshooter(hp, maxHP, atk, def)
    }
}

fun SquadTable.getAllies(userId: Int): List<CharUnit> {
    return innerJoin(UnitTable)
        .slice(UnitTable.columns)
        .select { SquadTable.user.eq(userId) }
        .map { it.toCharUnit() }
}

fun UnitTable.insertUnitAndGetId(unit: CharUnit): EntityID<Long> {
    return insertAndGetId {
        it[UnitTable.unit] = unit.enum
        it[UnitTable.hp] = unit.hp
        it[UnitTable.maxHP] = unit.maxHP
        it[UnitTable.atk] = unit.atk
        it[UnitTable.def] = unit.def
    }
}
