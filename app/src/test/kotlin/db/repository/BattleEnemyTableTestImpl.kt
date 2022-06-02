package com.bitwiserain.pbbg.app.test.db.repository

import com.bitwiserain.pbbg.app.db.repository.battle.BattleEnemyTable
import com.bitwiserain.pbbg.app.domain.model.MyUnit

class BattleEnemyTableTestImpl(private val units: MutableMap<Long, MyUnit>) : BattleEnemyTable {
    private val enemies = mutableMapOf<Long, Set<Long>>()

    override fun getEnemy(battleSession: Long, enemyId: Long): MyUnit? =
        units[enemyId]?.takeIf { enemies[battleSession]?.contains(enemyId) ?: false }

    override fun getEnemies(battleSession: Long): List<MyUnit> =
        enemies[battleSession]?.mapNotNull { units[it] } ?: emptyList()

    override fun insertEnemy(battleSession: Long, unitId: Long) {
        enemies.compute(battleSession) { _, enemyIds ->
            if (enemyIds != null) enemyIds + unitId else setOf(unitId)
        }
    }
}
