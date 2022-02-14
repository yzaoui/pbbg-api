package com.bitwiserain.pbbg.app.db.repository

import com.bitwiserain.pbbg.app.db.repository.UnitTable.UnitForm
import com.bitwiserain.pbbg.app.domain.model.MyUnit
import com.bitwiserain.pbbg.app.domain.model.MyUnitEnum
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

interface UnitTable {

    /**
     * Inserts a new unit and returns its ID.
     */
    fun insertUnitAndGetId(unit: UnitForm): Long

    /**
     * Updates a unit with new stats.
     */
    fun updateUnit(unitId: Long, unit: MyUnit)

    fun getUnit(unitId: Long): MyUnit?

    /**
     * The form of fields required to create a new unit.
     * The new unit will start with 0 exp, and be at full HP.
     */
    data class UnitForm(
        val enum: MyUnitEnum,
        val hp: Int,
        val atk: Int,
        val def: Int,
        val int: Int,
        val res: Int
    )
}

class UnitTableImpl : UnitTable {

    object Exposed : LongIdTable(name = "Unit") {

        val unit = enumeration("unit", MyUnitEnum::class)
        val hp = integer("hp")
        val maxHP = integer("max_hp")
        val atk = integer("atk")
        val def = integer("def")
        val int = integer("int")
        val res = integer("res")
        val exp = long("exp")
    }

    /**
     * Inserts a new unit and returns its ID.
     */
    override fun insertUnitAndGetId(unit: UnitForm): Long = Exposed.insertAndGetId {
        it[Exposed.unit] = unit.enum
        it[Exposed.hp] = unit.hp
        it[Exposed.maxHP] = unit.hp
        it[Exposed.atk] = unit.atk
        it[Exposed.def] = unit.def
        it[Exposed.int] = unit.int
        it[Exposed.res] = unit.res
        it[Exposed.exp] = 0L
    }.value

    /**
     * Updates a unit with new stats.
     */
    override fun updateUnit(unitId: Long, unit: MyUnit) {
        Exposed.update({ Exposed.id.eq(unitId) }) {
            it[Exposed.hp] = unit.hp
            it[Exposed.maxHP] = unit.maxHP
            it[Exposed.atk] = unit.atk
            it[Exposed.def] = unit.def
            it[Exposed.int] = unit.int
            it[Exposed.res] = unit.res
            it[Exposed.exp] = unit.exp
        }
    }

    override fun getUnit(unitId: Long): MyUnit? = Exposed
        .select { Exposed.id eq unitId }
        .singleOrNull()
        ?.toMyUnit()
}
