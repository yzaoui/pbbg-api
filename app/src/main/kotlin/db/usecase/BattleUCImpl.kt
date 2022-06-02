package com.bitwiserain.pbbg.app.db.usecase

import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.SquadTable
import com.bitwiserain.pbbg.app.db.repository.UnitTable
import com.bitwiserain.pbbg.app.db.repository.UnitTableImpl
import com.bitwiserain.pbbg.app.db.repository.battle.BattleEnemyTable
import com.bitwiserain.pbbg.app.db.repository.battle.BattleSessionTable
import com.bitwiserain.pbbg.app.db.repository.execAndMap
import com.bitwiserain.pbbg.app.domain.model.MyUnit
import com.bitwiserain.pbbg.app.domain.model.battle.Battle
import com.bitwiserain.pbbg.app.domain.model.battle.BattleAction
import com.bitwiserain.pbbg.app.domain.model.battle.BattleActionResult
import com.bitwiserain.pbbg.app.domain.model.battle.BattleReward
import com.bitwiserain.pbbg.app.domain.model.battle.UnitEffect
import com.bitwiserain.pbbg.app.domain.usecase.BattleUC
import com.bitwiserain.pbbg.app.domain.usecase.NoBattleInSessionException

class BattleUCImpl(
    private val transaction: Transaction,
    private val battleEnemyTable: BattleEnemyTable,
    private val battleSessionTable: BattleSessionTable,
    private val squadTable: SquadTable,
    private val unitTable: UnitTable,
) : BattleUC {

    override fun allyTurn(userId: Int, action: BattleAction): BattleActionResult = transaction {
        val battleSession = battleSessionTable.getBattleSessionId(userId) ?: throw NoBattleInSessionException()

        val queue = battleSessionTable.getBattleQueue(battleSession)

        // Ally should be next in queue
        val ally = squadTable.getAlly(userId, queue.peek()) ?: throw Exception()

        // Make sure this ally can perform this action
        // TODO: Only current action is attack, which is available to every unit

        return@transaction act(userId, battleSession, ally, action)
    }

    override fun enemyTurn(userId: Int): BattleActionResult = transaction {
        val battleSession = battleSessionTable.getBattleSessionId(userId) ?: throw NoBattleInSessionException()

        val queue = battleSessionTable.getBattleQueue(battleSession)

        // Enemy should be next in queue
        val enemy = battleEnemyTable.getEnemy(battleSession, queue.peek()) ?: throw Exception()

        // Pick an action
        // TODO: Only current action is attack, so pick a random target
        val action = BattleAction.Attack(squadTable.getAllies(userId).filter { it.alive }.random().id)

        return@transaction act(userId, battleSession, enemy, action)
    }

    private fun act(userId: Int, battleSession: Long, actingUnit: MyUnit, action: BattleAction): BattleActionResult {
        val queue = battleSessionTable.getBattleQueue(battleSession)
        val unitsToRemove: MutableList<Long> = mutableListOf()
        val effects: MutableMap<Long, UnitEffect> = mutableMapOf()
        var reward: BattleReward? = null

        when (action) {
            is BattleAction.Attack -> {
                // Unit should exist
                val target = unitTable.getUnit(action.targetUnitId) ?: throw Exception()
                // Can't attack self
                if (target.id == actingUnit.id) throw Exception()
                // Can't attack units not in this battle
                if (!getBattle(userId, battleSession).contains(target.id)) throw Exception()
                // Can't attack dead units
                if (target.dead) throw Exception()

                val (updatedTarget, damage) = target.receiveDamage(actingUnit.atk)
                unitTable.updateUnit(target.id, updatedTarget)

                // Gain experience if target is defeated
                if (updatedTarget.dead) {
                    val updatedActingUnit = actingUnit.gainExperience(2L)
                    unitTable.updateUnit(actingUnit.id, updatedActingUnit)

                    unitsToRemove.add(target.id)
                }

                effects[target.id] = UnitEffect.Health(damage)
            }
        }

        val updatedQueue = queue.endTurn(unitsToRemove)
        battleSessionTable.updateBattleQueue(battleSession, updatedQueue)

        val updatedBattle = getBattle(userId, battleSession)

        if (isBattleOver(updatedBattle)) {
            // TODO: reward should depend on win/loss
            reward = BattleReward(0, emptyMap())
            deleteBattle(updatedBattle, battleSession)
        }

        return BattleActionResult(updatedBattle, effects, reward)
    }

    private fun getBattle(userId: Int, battleSession: Long) = Battle(
        allies = squadTable.getAllies(userId),
        enemies = battleEnemyTable.getEnemies(battleSession),
        battleQueue = battleSessionTable.getBattleQueue(battleSession)
    )

    private fun isBattleOver(battle: Battle): Boolean {
        return battle.allies.none { it.alive } || battle.enemies.none { it.alive }
    }

    private fun deleteBattle(battle: Battle, battleSession: Long) = transaction {
        // Delete enemies, since they only exist within this battle
        val enemyIdCSV = battle.enemies.asSequence().map { it.id }.joinToString()

        battleSessionTable.deleteBattle(battleSession)

        "DELETE FROM ${UnitTableImpl.Exposed.tableName} WHERE ${UnitTableImpl.Exposed.id.name} IN ($enemyIdCSV)".execAndMap {}
    }
}
