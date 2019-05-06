package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.model.battle.Battle
import com.bitwiserain.pbbg.domain.model.battle.BattleAction
import com.bitwiserain.pbbg.domain.model.battle.Turn
import com.bitwiserain.pbbg.domain.usecase.BattleUC
import com.bitwiserain.pbbg.domain.usecase.NoAlliesAliveException
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.interceptSetUserOr401
import com.bitwiserain.pbbg.loggedInUserKey
import com.bitwiserain.pbbg.respondFail
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.view.model.BattleJSON
import com.bitwiserain.pbbg.view.model.TurnJSON
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.*

fun Route.battleAPI(userUC: UserUC, battleUC: BattleUC) = route("/battle") {
    interceptSetUserOr401(userUC)

    route("/session") {
        /**
         * On success:
         *   [BattleJSON] when a battle is in session.
         *   [null] when no battle is in session.
         */
        get {
            val loggedInUser = call.attributes[loggedInUserKey]

            val battle = battleUC.getCurrentBattle(loggedInUser.id)

            call.respondSuccess(battle?.toJSON())
        }

        param("action", "generate") {
            /**
             * On success:
             *   [BattleJSON]
             *
             * Error situations:
             *   [NoAlliesAliveException]
             */
            post {
                try {
                    val loggedInUser = call.attributes[loggedInUserKey]

                    val battle = battleUC.generateBattle(loggedInUser.id)

                    call.respondSuccess(battle.toJSON())
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
         *   [BattleJSON]
         */
        post {
            val loggedInUser = call.attributes[loggedInUserKey]

            val params = call.receive<AttackParams>()

            val battle = battleUC.allyTurn(loggedInUser.id, BattleAction.Attack(params.enemyId))

            call.respondSuccess(battle.toJSON())
        }
    }

    route("/enemyTurn") {
        post {
            val loggedInUser = call.attributes[loggedInUserKey]

            val battle = battleUC.enemyTurn(loggedInUser.id)

            call.respondSuccess(battle.toJSON())
        }
    }
}

private data class AttackParams(val enemyId: Long)

private fun Battle.toJSON() = BattleJSON(
    allies = allies.map { it.toJSON() },
    enemies = enemies.map { it.toJSON() },
    turns = battleQueue.turns.map { it.toJSON() }
)

private fun Turn.toJSON() = TurnJSON(
    unitId
)
