package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.db.form.MyUnitForm
import com.bitwiserain.pbbg.domain.model.MyUnit
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

object SquadTable : LongIdTable() {
    val user = reference("user_id", UserTable)
    val unit = reference("unit_id", UnitTable)

    /**
     * Gets a single ally in a user's squad.
     */
    fun getAlly(userId: Int, allyId: Long): MyUnit? {
        return innerJoin(UnitTable)
            .slice(UnitTable.columns)
            .select { SquadTable.user.eq(userId) and UnitTable.id.eq(allyId) }
            .singleOrNull()
            ?.toMyUnit()
    }

    /**
     * Gets all allies in a user's squad.
     */
    fun getAllies(userId: Int): List<MyUnit> {
        return innerJoin(UnitTable)
            .slice(UnitTable.columns)
            .select { SquadTable.user.eq(userId) }
            .map { it.toMyUnit() }
    }

    fun insertAllies(userId: EntityID<Int>, allies: List<MyUnitForm>) {
        for (ally in allies) {
            // Create enemy unit in unit table
            val allyId = UnitTable.insertUnitAndGetId(ally)

            // Connect newly created enemy to this battle session
            insert {
                it[SquadTable.user] = userId
                it[SquadTable.unit] = allyId
            }
        }
    }
}
