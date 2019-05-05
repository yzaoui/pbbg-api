package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.db.form.MyUnitForm
import com.bitwiserain.pbbg.domain.model.MyUnit
import com.bitwiserain.pbbg.domain.model.MyUnitEnum
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

object UnitTable : LongIdTable() {
    val unit = enumeration("unit", MyUnitEnum::class)
    val hp = integer("hp")
    val maxHP = integer("max_hp")
    val atk = integer("atk")
    val exp = long("exp")

    /**
     * Inserts a new unit and returns its ID.
     */
    fun insertUnitAndGetId(unit: MyUnitForm): EntityID<Long> {
        return insertAndGetId {
            it[UnitTable.unit] = unit.enum
            it[UnitTable.hp] = unit.hp
            it[UnitTable.maxHP] = unit.hp
            it[UnitTable.atk] = unit.atk
            it[UnitTable.exp] = 0L
        }
    }

    /**
     * Updates a unit with new stats.
     */
    fun updateUnit(unitId: Long, unit: MyUnit) {
        update({ UnitTable.id.eq(unitId) }) {
            it[UnitTable.hp] = unit.hp
            it[UnitTable.maxHP] = unit.maxHP
            it[UnitTable.atk] = unit.atk
            it[UnitTable.exp] = unit.exp
        }
    }

    fun getUnit(unitId: Long): MyUnit? {
        return select { id eq unitId }
            .singleOrNull()
            ?.toMyUnit()
    }
}
