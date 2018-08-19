package miner.route.api

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import miner.MinerSession
import miner.domain.usecase.EquipmentUC
import miner.domain.usecase.UserUC

@Location("/equipment")
class EquipmentAPILocation

fun Route.equipmentAPI(userUC: UserUC, equipmentUC: EquipmentUC) {
    get<EquipmentAPILocation> {
        // TODO: Remove cookie dependency
        val loggedInUser = call.sessions.get<MinerSession>()?.let { userUC.getUserById(it.userId) }
        if (loggedInUser == null) {
            call.respond(HttpStatusCode.Forbidden)
            return@get
        }

        //TODO: Return whole inventory, not just pickaxe
        val result = mutableMapOf<String, String>()
        equipmentUC.getPickaxe(loggedInUser.id)?.let { result["pickaxe"] = it.type }

        call.respond(result)
    }
}
