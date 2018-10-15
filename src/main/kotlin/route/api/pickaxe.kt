package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.model.Pickaxe
import com.bitwiserain.pbbg.domain.usecase.EquipmentUC
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.interceptSetUserOr401
import com.bitwiserain.pbbg.loggedInUserKey
import com.bitwiserain.pbbg.respondError
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.view.model.mine.PickaxeJSON
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

const val PICKAXE_PATH = "/pickaxe"
@Location(PICKAXE_PATH)
class PickaxeLocation

fun Route.pickaxe(userUC: UserUC, equipmentUC: EquipmentUC) {
    interceptSetUserOr401(userUC)

    route(PICKAXE_PATH) {
        get {
            val loggedInUser = call.attributes[loggedInUserKey]

            val pickaxe = equipmentUC.getEquippedPickaxe(loggedInUser.id)

            call.respondSuccess(pickaxe?.toJSON())
        }
    }
}

// TODO: Find appropriate place for this adapter
fun Pickaxe.toJSON() = PickaxeJSON(
    pickaxeKind = type,
    cells = cells.map { intArrayOf(it.first, it.second) }
)
