package com.bitwiserain.pbbg.app.route.api

import com.bitwiserain.pbbg.app.API_ROOT
import com.bitwiserain.pbbg.app.domain.UnitExperienceManager
import com.bitwiserain.pbbg.app.domain.model.MyUnit
import com.bitwiserain.pbbg.app.domain.model.Squad
import com.bitwiserain.pbbg.app.domain.usecase.SquadInBattleException
import com.bitwiserain.pbbg.app.domain.usecase.UnitUC
import com.bitwiserain.pbbg.app.respondFail
import com.bitwiserain.pbbg.app.respondSuccess
import com.bitwiserain.pbbg.app.user
import com.bitwiserain.pbbg.app.view.model.MyUnitJSON
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

fun Route.squadAPI(unitUC: UnitUC) = route("/squad") {
    /**
     * On success:
     *   [SquadJSON]
     */
    get {
        val squad = unitUC.getSquad(call.user.id)

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
                val healedSquad = unitUC.healSquad(call.user.id)

                call.respondSuccess(healedSquad.toJSON())
            } catch (e: SquadInBattleException) {
                call.respondFail("Can't heal squad while a battle is in session.")
            }
        }
    }
}

@Serializable
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
