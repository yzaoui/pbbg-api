package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.model.battle.*
import com.bitwiserain.pbbg.domain.usecase.BattleAlreadyInProgressException
import com.bitwiserain.pbbg.domain.usecase.BattleUC
import com.bitwiserain.pbbg.domain.usecase.NoAlliesAliveException
import com.bitwiserain.pbbg.respondFail
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.user
import com.bitwiserain.pbbg.view.model.battle.*
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.*

fun Route.battleAPI(battleUC: BattleUC) = route("/battle") {
    route("/session") {
        /**
         * On success:
         *   [BattleJSON] when a battle is in session.
         *   [null] when no battle is in session.
         */
        get {
            val battle = battleUC.getCurrentBattle(call.user.id)

            call.respondSuccess(battle?.toJSON())
        }

        param("action", "generate") {
            /**
             * On success:
             *   [BattleJSON]
             *
             * Error situations:
             *   [BattleAlreadyInProgressException]
             *   [NoAlliesAliveException]
             */
            post {
                try {
                    val battle = battleUC.generateBattle(call.user.id)

                    call.respondSuccess(battle.toJSON())
                } catch (e: BattleAlreadyInProgressException) {
                    call.respondFail("There is already a battle in progress.")
                } catch (e: NoAlliesAliveException) {
                    call.respondFail("Must have at least one unite alive to initiate battle.")
                }
            }
        }
    }

    route("/allyTurn") {
        /**
         * Expects body:
         *   [AttackParams]
         *
         * On success:
         *   [BattleActionResultJSON]
         */
        post {
            val params = call.receive<AttackParams>()

            val result = battleUC.allyTurn(call.user.id, BattleAction.Attack(params.targetUnitId))

            call.respondSuccess(result.toJSON())
        }
    }

    route("/enemyTurn") {
        /**
         * On success:
         *   [BattleActionResultJSON]
         */
        post {
            val result = battleUC.enemyTurn(call.user.id)

            call.respondSuccess(result.toJSON())
        }
    }
}

private data class AttackParams(val targetUnitId: Long)

private fun BattleActionResult.toJSON() = BattleActionResultJSON(
    battle = battle.toJSON(),
    unitEffects = unitEffects.mapValues { it.value.toJSON() },
    reward = reward?.toJSON()
)

private fun UnitEffect.toJSON() = when (this) {
    is UnitEffect.Health -> toJSON()
}

private fun UnitEffect.Health.toJSON() = UnitEffectJSON.HealthJSON(
    delta = delta
)

private fun BattleReward.toJSON() = BattleRewardJSON(
    gold = gold,
    items = items.map { it.value.toJSON(it.key) }
)

private fun Battle.toJSON() = BattleJSON(
    allies = allies.map { it.toJSON() },
    enemies = enemies.map { it.toJSON() },
    turns = battleQueue.turns.map { it.toJSON() }
)

private fun Turn.toJSON() = TurnJSON(
    unitId
)
