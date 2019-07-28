package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.form.MyUnitForm
import com.bitwiserain.pbbg.db.repository.SquadTable
import com.bitwiserain.pbbg.db.repository.UnitTable
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.db.repository.battle.BattleEnemyTable
import com.bitwiserain.pbbg.db.repository.battle.BattleSessionTable
import com.bitwiserain.pbbg.db.repository.execAndMap
import com.bitwiserain.pbbg.domain.model.MyUnit
import com.bitwiserain.pbbg.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.domain.model.battle.*
import com.bitwiserain.pbbg.domain.usecase.BattleAlreadyInProgressException
import com.bitwiserain.pbbg.domain.usecase.BattleUC
import com.bitwiserain.pbbg.domain.usecase.NoAlliesAliveException
import com.bitwiserain.pbbg.domain.usecase.NoBattleInSessionException
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.random.Random

class BattleUCImpl(private val db: Database) : BattleUC {
    override fun getCurrentBattle(userId: Int): Battle? = transaction(db) {
        return@transaction BattleSessionTable.getBattleSessionId(userId)?.let { battleSession ->
            getBattle(userId, battleSession)
        }
    }

    override fun generateBattle(userId: Int): Battle = transaction(db) {
        if (BattleSessionTable.isBattleInProgress(userId)) throw BattleAlreadyInProgressException()

        val allies = SquadTable.getAllies(userId)

        // There must be allies alive to start a battle
        if (allies.none { it.alive }) throw NoAlliesAliveException()

        val battleSession = BattleSessionTable.insertAndGetId {
            it[BattleSessionTable.userId] = EntityID(userId, UserTable)
            it[BattleSessionTable.battleQueue] = ""
        }

        val newEnemies = mutableListOf<MyUnitForm>()
        // Add 1-3 new enemies
        for (i in 0..Random.nextInt(1, 3)) {
            newEnemies.add(MyUnitForm(MyUnitEnum.values().random(), Random.nextInt(7, 12), Random.nextInt(1, 3), Random.nextInt(1, 3)))
        }
        BattleEnemyTable.insertEnemies(battleSession, newEnemies)

        val enemies = BattleEnemyTable.getEnemies(battleSession)

        val battleQueue = BattleQueue(
            turns = (allies + enemies).filter { it.alive }.map { Turn(it.id, Random.nextInt(100)) }.sortedByDescending { it.counter }
        )

        BattleSessionTable.updateBattleQueue(battleSession, battleQueue)

        Battle(allies = allies, enemies = enemies, battleQueue = battleQueue)
    }

    override fun allyTurn(userId: Int, action: BattleAction): BattleActionResult = transaction(db) {
        val battleSession = BattleSessionTable.getBattleSessionId(userId) ?: throw NoBattleInSessionException()

        val queue = BattleSessionTable.getBattleQueue(battleSession)

        // Ally should be next in queue
        val ally = SquadTable.getAlly(userId, queue.peek()) ?: throw Exception()

        // Make sure this ally can perform this action
        // TODO: Only current action is attack, which is available to every unit

        return@transaction act(userId, battleSession, ally, action)
    }

    override fun enemyTurn(userId: Int): BattleActionResult = transaction(db) {
        val battleSession = BattleSessionTable.getBattleSessionId(userId) ?: throw NoBattleInSessionException()

        val queue = BattleSessionTable.getBattleQueue(battleSession)

        // Enemy should be next in queue
        val enemy = BattleEnemyTable.getEnemy(battleSession, queue.peek()) ?: throw Exception()

        // Pick an action
        // TODO: Only current action is attack, so pick a random target
        val action = BattleAction.Attack(SquadTable.getAllies(userId).filter { it.alive }.random().id)

        return@transaction act(userId, battleSession, enemy, action)
    }

    private fun act(userId: Int, battleSession: EntityID<Long>, actingUnit: MyUnit, action: BattleAction): BattleActionResult {
        val queue = BattleSessionTable.getBattleQueue(battleSession)
        val unitsToRemove: MutableList<Long> = mutableListOf()
        val effects: MutableMap<Long, UnitEffect> = mutableMapOf()
        var reward: BattleReward? = null

        when (action) {
            is BattleAction.Attack -> {
                // Unit should exist
                val target = UnitTable.getUnit(action.targetUnitId) ?: throw Exception()
                // Can't attack self
                if (target.id == actingUnit.id) throw Exception()
                // Can't attack units not in this battle
                if (!getBattle(userId, battleSession).contains(target.id)) throw Exception()
                // Can't attack dead units
                if (target.dead) throw Exception()

                val updatedTarget = target.receiveDamage(actingUnit.atk)
                UnitTable.updateUnit(target.id, updatedTarget)

                // Gain experience if target is defeated
                if (updatedTarget.dead) {
                    val updatedActingUnit = actingUnit.gainExperience(2L)
                    UnitTable.updateUnit(actingUnit.id, updatedActingUnit)

                    unitsToRemove.add(target.id)
                }

                effects[target.id] = UnitEffect.Health(-actingUnit.atk)
            }
            else -> throw RuntimeException() // All actions should be accounted for
        }

        val updatedQueue = queue.endTurn(unitsToRemove)
        BattleSessionTable.updateBattleQueue(battleSession, updatedQueue)

        val updatedBattle = getBattle(userId, battleSession)

        if (isBattleOver(updatedBattle)) {
            // TODO: reward should depend on win/loss
            reward = BattleReward(0, emptyList())
            deleteBattle(updatedBattle, battleSession)
        }

        return BattleActionResult(updatedBattle, effects, reward)
    }

    private fun getBattle(userId: Int, battleSession: EntityID<Long>) = Battle(
        allies = SquadTable.getAllies(userId),
        enemies = BattleEnemyTable.getEnemies(battleSession),
        battleQueue = BattleSessionTable.getBattleQueue(battleSession)
    )

    private fun isBattleOver(battle: Battle): Boolean {
        return battle.allies.none { it.alive } || battle.enemies.none { it.alive }
    }

    private fun deleteBattle(battle: Battle, battleSession: EntityID<Long>) = transaction(db) {
        // Delete enemies, since they only exist within this battle
        val enemyIdCSV = battle.enemies.asSequence().map { it.id }.joinToString()

        BattleSessionTable.deleteBattle(battleSession)

        "DELETE FROM ${UnitTable.tableName} WHERE ${UnitTable.id.name} IN ($enemyIdCSV)".execAndMap {}
    }
}
