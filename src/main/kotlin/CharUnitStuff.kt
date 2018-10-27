package com.bitwiserain.pbbg

import com.bitwiserain.pbbg.CharUnit.*
import com.bitwiserain.pbbg.CharUnitEnum.*
import com.bitwiserain.pbbg.db.repository.UserTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

sealed class CharUnit {
    abstract val enum: CharUnitEnum
    abstract val atk: Int
    abstract val def: Int

    class IceCreamWizard(override val atk: Int, override val def: Int) : CharUnit() {
        override val enum get() = ICE_CREAM_WIZARD
    }

    class Twolip(override val atk: Int, override val def: Int) : CharUnit() {
        override val enum get() = TWOLIP
    }

    class Carpshooter(override val atk: Int, override val def: Int) : CharUnit() {
        override val enum get() = CARPSHOOTER
    }
}

enum class CharUnitEnum {
    ICE_CREAM_WIZARD,
    TWOLIP,
    CARPSHOOTER
}

object SquadTable : LongIdTable() {
    val userId = reference("user_id", UserTable)
    val unit = enumeration("unit", CharUnitEnum::class)
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
    SquadTable.insert {
        it[SquadTable.userId] = user
        it[SquadTable.unit] = unit.enum
        it[SquadTable.atk] = unit.atk
        it[SquadTable.def] = unit.def
    }
}

private fun ResultRow.toCharUnit(): CharUnit {
    val unitEnum = this[SquadTable.unit]
    val atk = this[SquadTable.atk]
    val def = this[SquadTable.def]

    return when (unitEnum) {
        ICE_CREAM_WIZARD -> IceCreamWizard(atk, def)
        TWOLIP -> Twolip(atk, def)
        CARPSHOOTER -> Carpshooter(atk, def)
    }
}

fun SquadTable.getAllies(userId: Int): List<CharUnit> {
    return select { SquadTable.userId.eq(userId) }
        .map { it.toCharUnit() }
}
