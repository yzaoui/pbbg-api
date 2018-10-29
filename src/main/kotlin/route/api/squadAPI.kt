package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.*
import com.bitwiserain.pbbg.domain.usecase.UserUC
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.squadAPI(userUC: UserUC, unitUC: UnitUC) = route("/squad") {
    interceptSetUserOr401(userUC)

    get {
        val loggedInUser = call.attributes[loggedInUserKey]

        val squad = unitUC.getSquad(loggedInUser.id)

        call.respondSuccess(squad.toJSON())
    }
}

class SquadJSON(
    val units: List<CharUnitJSON>
)

class CharUnitJSON(
    val id: Long,
    val baseUnitId: Int,
    val hp: Int,
    val maxHP: Int,
    val atk: Int
)

fun Squad.toJSON() = SquadJSON(
    units = units.map { it.toJSON() }
)

fun CharUnit.toJSON() = CharUnitJSON(
    id = id,
    baseUnitId = enum.ordinal,
    hp = hp,
    maxHP = maxHP,
    atk = atk
)
