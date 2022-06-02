package com.bitwiserain.pbbg.app.db.repository.battle

import com.bitwiserain.pbbg.app.db.repository.UnitTableImpl
import com.bitwiserain.pbbg.app.db.repository.toMyUnit
import com.bitwiserain.pbbg.app.domain.model.MyUnit
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

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
     * Inserts new enemy unit into a battle session.
     */
    fun insertEnemy(battleSession: Long, unitId: Long)
}

class BattleEnemyTableImpl : BattleEnemyTable {

    object Exposed : Table(name = "BattleEnemy") {

        val battle = reference("battle_session_id", BattleSessionTableImpl.Exposed, ReferenceOption.CASCADE)
        val unit = reference("unit_id", UnitTableImpl.Exposed, ReferenceOption.CASCADE)
    }

    override fun getEnemy(battleSession: Long, enemyId: Long): MyUnit? {
        return Exposed.innerJoin(UnitTableImpl.Exposed)
            .slice(UnitTableImpl.Exposed.columns)
            .select { Exposed.battle.eq(battleSession) and UnitTableImpl.Exposed.id.eq(enemyId) }
            .singleOrNull()
            ?.toMyUnit()
    }

    override fun getEnemies(battleSession: Long): List<MyUnit> {
        return Exposed.innerJoin(UnitTableImpl.Exposed)
            .slice(UnitTableImpl.Exposed.columns)
            .select { Exposed.battle.eq(battleSession) }
            .map { it.toMyUnit() }
    }

    override fun insertEnemy(battleSession: Long, unitId: Long) {
        Exposed.insert {
            it[Exposed.battle] = EntityID(battleSession, BattleSessionTableImpl.Exposed)
            it[Exposed.unit] = EntityID(unitId, UnitTableImpl.Exposed)
        }
    }
}
