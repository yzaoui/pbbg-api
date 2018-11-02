package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.*
import com.bitwiserain.pbbg.db.form.MyUnitForm
import com.bitwiserain.pbbg.db.repository.SquadTable
import com.bitwiserain.pbbg.db.repository.UnitTable
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.db.repository.battle.BattleEnemyTable
import com.bitwiserain.pbbg.db.repository.battle.BattleSessionTable
import com.bitwiserain.pbbg.domain.model.Battle
import com.bitwiserain.pbbg.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.domain.usecase.BattleUC
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.random.Random

class BattleUCImpl(private val db: Database) : BattleUC {
    override fun getCurrentBattle(userId: Int): Battle? = transaction(db) {
        val battleSession = BattleSessionTable.getBattleSessionId(userId)

        if (battleSession == null) return@transaction null

        Battle(allies = SquadTable.getAllies(userId), enemies = BattleEnemyTable.getEnemies(battleSession))
    }

    override fun generateBattle(userId: Int): Battle = transaction(db) {
        // TODO: Forbid action if a battle is already in progress

        val battleSession = BattleSessionTable.insertAndGetId {
            it[BattleSessionTable.userId] = EntityID(userId, UserTable)
        }

        val newEnemies = mutableListOf<MyUnitForm>()
        // Add 1-3 new enemies
        for (i in 0..Random.nextInt(1, 3)) {
            newEnemies.add(MyUnitForm(MyUnitEnum.values().random(), Random.nextInt(7, 12), 0))
        }
        BattleEnemyTable.insertEnemies(battleSession, newEnemies)

        val allies = SquadTable.getAllies(userId)
        val enemies = BattleEnemyTable.getEnemies(battleSession)

        Battle(allies = allies, enemies = enemies)
    }

    override fun attack(userId: Int, allyId: Long, enemyId: Long): Battle = transaction(db) {
        val battleSession = BattleSessionTable.getBattleSessionId(userId) ?: throw Exception()

        // Ally should exist
        val ally = SquadTable.getAlly(userId, allyId) ?: throw Exception()

        // Enemy should exist
        val enemy = BattleEnemyTable.getEnemy(battleSession, enemyId) ?: throw Exception()

        // Enemy should not already be dead
        if (enemy.dead) throw Exception()

        // Apply damage to attacked enemy and update unit
        val updatedEnemy = enemy.receiveDamage(ally.atk)
        UnitTable.updateUnit(enemyId, updatedEnemy)

        // Gain experience if ally killed enemy
        if (updatedEnemy.dead) {
            val updatedAlly = ally.gainExperience(2L)
            UnitTable.updateUnit(allyId, updatedAlly)
        }

        val updatedBattle = Battle(allies = SquadTable.getAllies(userId), enemies = BattleEnemyTable.getEnemies(battleSession))

        deleteBattleIfOver(updatedBattle, battleSession)

        return@transaction updatedBattle
    }

    private fun deleteBattleIfOver(battle: Battle, battleSession: EntityID<Long>) = transaction(db) {
        val aliveEnemies = battle.enemies.filter { it.hp > 0 }

        if (aliveEnemies.isEmpty()) {
            // All enemies are defeated

            val enemyIdCSV = battle.enemies.asSequence().map { it.id }.joinToString()

            BattleSessionTable.deleteBattle(battleSession)

            "DELETE FROM ${UnitTable.tableName} WHERE ${UnitTable.id.name} IN ($enemyIdCSV)".execAndMap {}
        }
    }
}
