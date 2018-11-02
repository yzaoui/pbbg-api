package com.bitwiserain.pbbg.db.repository.battle

import com.bitwiserain.pbbg.db.form.MyUnitForm
import com.bitwiserain.pbbg.db.repository.UnitTable
import com.bitwiserain.pbbg.domain.model.MyUnit
import com.bitwiserain.pbbg.insertUnitAndGetId
import com.bitwiserain.pbbg.toMyUnit
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*

object BattleEnemyTable : Table() {
    val battle = reference("battle_session_id", BattleSessionTable, ReferenceOption.CASCADE)
    val unit = reference("unit_id", UnitTable, ReferenceOption.CASCADE)

    /**
     * Get a single enemy belonging to a battle session, if any.
     */
    fun getEnemy(battleSession: EntityID<Long>, enemyId: Long): MyUnit? {
        return innerJoin(UnitTable)
            .slice(UnitTable.columns)
            .select { BattleEnemyTable.battle.eq(battleSession) and UnitTable.id.eq(enemyId) }
            .singleOrNull()
            ?.toMyUnit()
    }

    /**
     * Get a list of enemies belonging to a battle session, if any.
     */
    fun getEnemies(battleSession: EntityID<Long>): List<MyUnit> {
        return innerJoin(UnitTable)
            .slice(UnitTable.columns)
            .select { BattleEnemyTable.battle.eq(battleSession) }
            .map { it.toMyUnit() }
    }

    /**
     * Insert new enemy units into a battle session.
     */
    fun insertEnemies(battleSession: EntityID<Long>, enemies: List<MyUnitForm>) {
        // TODO: There's gotta be a way to do this in batch :/
        for (enemy in enemies) {
            // Create enemy unit in unit table
            val enemyId = UnitTable.insertUnitAndGetId(enemy)

            // Connect newly created enemy to this battle session
            insert {
                it[BattleEnemyTable.battle] = battleSession
                it[BattleEnemyTable.unit] = enemyId
            }
        }
    }
}
