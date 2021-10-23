package com.bitwiserain.pbbg.db.repository.battle

import com.bitwiserain.pbbg.db.repository.UnitForm
import com.bitwiserain.pbbg.db.repository.UnitTable
import com.bitwiserain.pbbg.db.repository.toMyUnit
import com.bitwiserain.pbbg.domain.model.MyUnit
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

interface BattleEnemyTable {

    /**
     * Gets a single enemy belonging to a battle session, if any.
     */
    fun getEnemy(battleSession: Long, enemyId: Long): MyUnit?

    /**
     * Gets a list of enemies belonging to a battle session, if any.
     */
    fun getEnemies(battleSession: Long): List<MyUnit>

    /**
     * Inserts new enemy units into a battle session.
     */
    fun insertEnemies(battleSession: Long, enemies: List<UnitForm>)
}

class BattleEnemyTableImpl : BattleEnemyTable {

    object Exposed : Table(name = "BattleEnemy") {

        val battle = reference("battle_session_id", BattleSessionTable, ReferenceOption.CASCADE)
        val unit = reference("unit_id", UnitTable, ReferenceOption.CASCADE)
    }

    override fun getEnemy(battleSession: Long, enemyId: Long): MyUnit? {
        return Exposed.innerJoin(UnitTable)
            .slice(UnitTable.columns)
            .select { Exposed.battle.eq(battleSession) and UnitTable.id.eq(enemyId) }
            .singleOrNull()
            ?.toMyUnit()
    }

    override fun getEnemies(battleSession: Long): List<MyUnit> {
        return Exposed.innerJoin(UnitTable)
            .slice(UnitTable.columns)
            .select { Exposed.battle.eq(battleSession) }
            .map { it.toMyUnit() }
    }

    override fun insertEnemies(battleSession: Long, enemies: List<UnitForm>) {
        // TODO: There's gotta be a way to do this in batch :/
        for (enemy in enemies) {
            // Create enemy unit in unit table
            val enemyId = UnitTable.insertUnitAndGetId(enemy)

            // Connect newly created enemy to this battle session
            Exposed.insert {
                it[Exposed.battle] = EntityID(battleSession, BattleSessionTable)
                it[Exposed.unit] = EntityID(enemyId, UnitTable)
            }
        }
    }
}
