package pbbg.route.api

import data.model.Pickaxe
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import pbbg.domain.usecase.EquipmentUC
import pbbg.domain.usecase.UserUC
import pbbg.interceptSetUserOr401
import pbbg.loggedInUserKey
import pbbg.respondError
import pbbg.respondSuccess

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

            call.respondSuccess(pickaxe?.toJSON())
        }

        //TODO: Remove this, only temporarily randomly generating a pickaxe
        post {
            val loggedInUser = call.attributes[loggedInUserKey]

            val pickaxe = equipmentUC.generatePickaxe(loggedInUser.id)
            if (pickaxe != null) {
                call.respondSuccess(pickaxe.toItem().toJSON())
            } else {
                call.respondError(HttpStatusCode.InternalServerError)
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

data class PickaxeJSON(val id: Int, val type: String, val cells: List<IntArray>)
fun Pickaxe.toJSON() = PickaxeJSON(this.ordinal, type, cells.map { kotlin.intArrayOf(it.first, it.second) })
