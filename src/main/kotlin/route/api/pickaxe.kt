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
import miner.respondFail
import miner.respondSuccess
import route.web.LoginLocation
import java.util.*

@Location("/pickaxe")
class PickaxeLocation

@Location("/pickaxe/all")
class PickaxeAllLocation

fun Route.pickaxe(userUC: UserUC, equipmentUC: EquipmentUC) {
    get<PickaxeLocation> {
        // TODO: Remove cookie dependency
        val loggedInUser = call.sessions.get<MinerSession>()?.let { userUC.getUserById(it.userId) }
        if (loggedInUser == null) {
            call.respondFail(HttpStatusCode.Unauthorized)
            return@get
        }

        val pickaxe = equipmentUC.getPickaxe(loggedInUser.id)
        if (pickaxe == null) {
            call.respond(HttpStatusCode.InternalServerError)
            return@get
        }

        call.respond(pickaxe.toJSON())
    }

    get<PickaxeAllLocation> {
        // TODO: Remove cookie dependency
        val loggedInUser = call.sessions.get<MinerSession>()?.let { userUC.getUserById(it.userId) }
        if (loggedInUser == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return@get
        }

        val pickaxes = equipmentUC.getAllPickaxes().map { it.toJSON() }
        call.respondSuccess(pickaxes)
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

data class PickaxeJSON(val id: Int, val type: String, val tiles: List<IntArray>)
fun Pickaxe.toJSON() = PickaxeJSON(this.ordinal, type, tiles.map { kotlin.intArrayOf(it.first, it.second) })
