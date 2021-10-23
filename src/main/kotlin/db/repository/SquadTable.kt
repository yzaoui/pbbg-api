package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.MyUnit
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.select

interface SquadTable {

    /**
     * Gets a single ally in a user's squad.
     */
    fun getAlly(userId: Int, allyId: Long): MyUnit?

    /**
     * Gets all allies in a user's squad.
     */
    fun getAllies(userId: Int): List<MyUnit>

    fun insertUnits(userId: Int, unitIds: Iterable<Long>)
}

class SquadTableImpl : SquadTable {

    object Exposed : LongIdTable(name = "Squad") {

        val user = reference("user_id", UserTable)
        val unit = reference("unit_id", UnitTable)
    }

    override fun getAlly(userId: Int, allyId: Long): MyUnit? = Exposed
        .innerJoin(UnitTable)
        .slice(UnitTable.columns)
        .select { Exposed.user.eq(userId) and UnitTable.id.eq(allyId) }
        .singleOrNull()
        ?.toMyUnit()

    override fun getAllies(userId: Int): List<MyUnit> = Exposed
        .innerJoin(UnitTable)
        .slice(UnitTable.columns)
        .select { Exposed.user.eq(userId) }
        .map { it.toMyUnit() }

    override fun insertUnits(userId: Int, unitIds: Iterable<Long>) {
        Exposed.batchInsert(unitIds) { unitId ->
            this[Exposed.user] = EntityID(userId, UserTable)
            this[Exposed.unit] = EntityID(unitId, UnitTable)
        }
    }
}
