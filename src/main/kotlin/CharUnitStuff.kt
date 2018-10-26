package com.bitwiserain.pbbg

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
}

enum class CharUnitEnum {
    ICE_CREAM_WIZARD
}

object CharUnitTable : LongIdTable() {
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
        val units = CharUnitTable.select { CharUnitTable.userId.eq(userId) }
            .map { it.toCharUnit() }

        Squad(units)
    }
}

class Squad(val units: List<CharUnit>)

fun addUnitToSquad(user: EntityID<Int>, unit: CharUnit) {
    CharUnitTable.insert {
        it[CharUnitTable.userId] = user
        it[CharUnitTable.unit] = unit.enum
        it[CharUnitTable.atk] = unit.atk
        it[CharUnitTable.def] = unit.def
    }
}

fun ResultRow.toCharUnit(): CharUnit {
    val unitEnum = this[CharUnitTable.unit]
    val atk = this[CharUnitTable.atk]
    val def = this[CharUnitTable.def]

    return when (unitEnum) {
        ICE_CREAM_WIZARD -> CharUnit.IceCreamWizard(atk, def)
    }
}
