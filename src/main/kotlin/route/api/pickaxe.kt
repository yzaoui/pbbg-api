package miner.route.api

import data.model.Pickaxe
import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respond
import io.ktor.routing.Route
import java.util.*

@Location("/pickaxe")
class PickaxeLocation

fun Route.pickaxe() {
    get<PickaxeLocation> {
        call.respond(Pickaxe.values()[Random().nextInt(Pickaxe.values().size)].toJSON())
    }
}

data class PickaxeJSON(val type: String, val tiles: List<IntArray>)

fun Pickaxe.toJSON() = PickaxeJSON(type, tiles.map { kotlin.intArrayOf(it.first, it.second) })
