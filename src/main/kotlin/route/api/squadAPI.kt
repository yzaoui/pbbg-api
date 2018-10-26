package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.*
import com.bitwiserain.pbbg.domain.usecase.UserUC
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.squadAPI(userUC: UserUC, unitUC: UnitUC) {
    interceptSetUserOr401(userUC)

    route("/squad") {
        get {
            val loggedInUser = call.attributes[loggedInUserKey]

            val squad = unitUC.getSquad(loggedInUser.id)

            call.respondSuccess(squad.toJSON())
        }
    }
}

class SquadJSON(
    val units: List<CharUnitJSON>
)

class CharUnitJSON(
    val baseUnitId: Int,
    val atk: Int,
    val def: Int
)

fun Squad.toJSON() = SquadJSON(
    units = units.map {
        CharUnitJSON(
            baseUnitId = it.enum.ordinal,
            atk = it.atk,
            def = it.def
        )
    }
)
