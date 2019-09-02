package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.*
import com.bitwiserain.pbbg.domain.UnitExperienceManager
import com.bitwiserain.pbbg.domain.model.MyUnit
import com.bitwiserain.pbbg.domain.model.Squad
import com.bitwiserain.pbbg.domain.usecase.SquadInBattleException
import com.bitwiserain.pbbg.domain.usecase.UnitUC
import com.bitwiserain.pbbg.view.model.MyUnitJSON
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

fun Route.squadAPI(unitUC: UnitUC) = route("/squad") {
    /**
     * On success:
     *   [SquadJSON]
     */
    get {
        val loggedInUser = call.user

        val squad = unitUC.getSquad(loggedInUser.id)

        call.respondSuccess(squad.toJSON())
    }

    route("/heal") {
        /**
         * On success:
         *   [SquadJSON]
         *
         * Error situations:
         *   [SquadInBattleException]
         */
        post {
            try {
                val loggedInUser = call.user

                val healedSquad = unitUC.healSquad(loggedInUser.id)

                call.respondSuccess(healedSquad.toJSON())
            } catch (e: SquadInBattleException) {
                call.respondFail("Can't heal squad while a battle is in session.")
            }
        }
    }
}

private class SquadJSON(
    val units: List<MyUnitJSON>
)

private fun Squad.toJSON() = SquadJSON(
    units = units.map { it.toJSON() }
)

fun MyUnit.toJSON() = MyUnitJSON(
    id = id,
    name = enum.friendlyName,
    baseUnitId = enum.ordinal,
    hp = hp,
    maxHP = maxHP,
    atk = atk,
    def = def,
    levelProgress = UnitExperienceManager.getLevelProgress(exp).toJSON(),
    idleAnimationURL = "$API_ROOT/img/unit/${enum.spriteName}.gif",
    iconURL = "$API_ROOT/img/unit-icon/${enum.spriteName}.png"
)
