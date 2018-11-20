package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.UnitExperienceManager
import com.bitwiserain.pbbg.domain.model.MyUnit
import com.bitwiserain.pbbg.domain.model.Squad
import com.bitwiserain.pbbg.domain.usecase.UnitUC
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.interceptSetUserOr401
import com.bitwiserain.pbbg.loggedInUserKey
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.view.model.MyUnitJSON
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.squadAPI(userUC: UserUC, unitUC: UnitUC) = route("/squad") {
    interceptSetUserOr401(userUC)

    /**
     * On success:
     *   [SquadJSON]
     */
    get {
        val loggedInUser = call.attributes[loggedInUserKey]

        val squad = unitUC.getSquad(loggedInUser.id)

        call.respondSuccess(squad.toJSON())
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
    name = name,
    baseUnitId = enum.ordinal,
    hp = hp,
    maxHP = maxHP,
    atk = atk,
    levelProgress = UnitExperienceManager.getLevelProgress(exp).toJSON(),
    idleAnimationURL = "/img/unit/$spriteName.gif"
)
