package miner.route.api

import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import miner.domain.usecase.EquipmentUC
import miner.domain.usecase.UserUC
import miner.interceptSetUserOr401
import miner.loggedInUserKey

@Location("/equipment")
class EquipmentAPILocation

fun Route.equipmentAPI(userUC: UserUC, equipmentUC: EquipmentUC) = route("/equipment") {
    interceptSetUserOr401(userUC)
    get {
        val loggedInUser = call.attributes[loggedInUserKey]

        //TODO: Return whole inventory, not just pickaxe
        val pickaxe = equipmentUC.getPickaxe(loggedInUser.id)

        call.respond(EquipmentJSON(pickaxe?.type))
    }
}

data class EquipmentJSON(val pickaxe: String?)
