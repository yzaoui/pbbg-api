package miner.route.api

import data.model.Pickaxe
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import miner.MinerSession
import miner.domain.usecase.EquipmentUC
import miner.domain.usecase.UserUC
import miner.href
import route.web.LoginLocation
import java.util.*

@Location("/pickaxe")
class PickaxeLocation

fun Route.pickaxe(userUC: UserUC, equipmentUC: EquipmentUC) {
    get<PickaxeLocation> {
        // TODO: Remove cookie dependency
        val loggedInUser = call.sessions.get<MinerSession>()?.let { userUC.getUserById(it.userId) }
        if (loggedInUser == null) {
            call.respond(HttpStatusCode.Forbidden)
            return@get
        }

        val pickaxe = equipmentUC.getPickaxe(loggedInUser.id) ?: return@get

        call.respond(pickaxe.toJSON())
    }

    /**
     * Generate and return pickaxe for this user
     */
    post<PickaxeLocation> {
        // TODO: Remove cookie dependency
        val loggedInUser = call.sessions.get<MinerSession>()?.let { userUC.getUserById(it.userId) }
        if (loggedInUser == null) {
            call.respond(HttpStatusCode.Forbidden)
            return@post
        }

        val pickaxe = equipmentUC.generatePickaxe(loggedInUser.id)
        if (pickaxe != null) {
            call.respond(pickaxe.toJSON())
        } else {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}

data class PickaxeJSON(val type: String, val tiles: List<IntArray>)

fun Pickaxe.toJSON() = PickaxeJSON(type, tiles.map { kotlin.intArrayOf(it.first, it.second) })
