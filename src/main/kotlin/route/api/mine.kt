package miner.route.api

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.request.ContentTransformationException
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import miner.*
import miner.domain.usecase.MiningUC
import miner.domain.usecase.UserUC

@Location("/mine")
class MineAPILocation

data class MinePositionParams(val x: Int, val y: Int)

fun Route.mine(userUC: UserUC, miningUC: MiningUC) = route("/mine") {
    interceptSetUserOr401(userUC)
    post {
        try {
            // TODO: Remove cookie dependency
            val loggedInUser = call.attributes[loggedInUserKey]

            val (x: Int, y: Int)= call.receive(MinePositionParams::class)

            val results = miningUC.mine(loggedInUser.id, x, y)
            if (results != null) {
                call.respondSuccess(results)
            } else {
                call.respond(HttpStatusCode.Accepted)
            }
        } catch (e: ContentTransformationException) {
            call.respondFail(HttpStatusCode.BadRequest, "Missing or invalid parameters.")
        } catch (e: Exception) {
            call.respondError(HttpStatusCode.InternalServerError, e.message.orEmpty())
        }
    }
}
