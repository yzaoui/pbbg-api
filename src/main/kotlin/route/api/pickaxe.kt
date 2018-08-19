package miner.route.api

import data.model.Pickaxe
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import miner.domain.usecase.EquipmentUC
import miner.domain.usecase.UserUC
import miner.interceptSetUserOr401
import miner.loggedInUserKey
import miner.respondSuccess

const val PICKAXE_PATH = "/pickaxe"
@Location(PICKAXE_PATH)
class PickaxeLocation

const val PICKAXE_ALL_PATH = "$PICKAXE_PATH/all"
@Location(PICKAXE_ALL_PATH)
class PickaxeAllLocation

fun Route.pickaxe(userUC: UserUC, equipmentUC: EquipmentUC) {
    interceptSetUserOr401(userUC)

    route(PICKAXE_PATH) {
        get {
            val loggedInUser = call.attributes[loggedInUserKey]

            val pickaxe = equipmentUC.getPickaxe(loggedInUser.id)
            if (pickaxe == null) {
                call.respond(HttpStatusCode.InternalServerError)
                return@get
            }

            call.respond(pickaxe.toJSON())
        }

        post {
            val loggedInUser = call.attributes[loggedInUserKey]

            val pickaxe = equipmentUC.generatePickaxe(loggedInUser.id)
            if (pickaxe != null) {
                call.respond(pickaxe.toJSON())
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }

    route(PICKAXE_ALL_PATH) {
        get {
            val pickaxes = equipmentUC.getAllPickaxes().map { it.toJSON() }
            call.respondSuccess(pickaxes)
        }
    }
}

data class PickaxeJSON(val id: Int, val type: String, val tiles: List<IntArray>)
fun Pickaxe.toJSON() = PickaxeJSON(this.ordinal, type, tiles.map { kotlin.intArrayOf(it.first, it.second) })
