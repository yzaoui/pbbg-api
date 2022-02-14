package com.bitwiserain.pbbg.app.db.repository

import com.bitwiserain.pbbg.app.domain.model.MyUnit
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

        val user = reference("user_id", UserTableImpl.Exposed)
        val unit = reference("unit_id", UnitTableImpl.Exposed)
    }

    override fun getAlly(userId: Int, allyId: Long): MyUnit? = Exposed
        .innerJoin(UnitTableImpl.Exposed)
        .slice(UnitTableImpl.Exposed.columns)
        .select { Exposed.user.eq(userId) and UnitTableImpl.Exposed.id.eq(allyId) }
        .singleOrNull()
        ?.toMyUnit()

    override fun getAllies(userId: Int): List<MyUnit> = Exposed
        .innerJoin(UnitTableImpl.Exposed)
        .slice(UnitTableImpl.Exposed.columns)
        .select { Exposed.user.eq(userId) }
        .map { it.toMyUnit() }

    override fun insertUnits(userId: Int, unitIds: Iterable<Long>) {
        Exposed.batchInsert(unitIds) { unitId ->
            this[Exposed.user] = EntityID(userId, UserTableImpl.Exposed)
            this[Exposed.unit] = EntityID(unitId, UnitTableImpl.Exposed)
        }
    }
}
