package miner.route.api

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.ContentTransformationException
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import miner.MinerSession
import miner.domain.usecase.MiningUC
import miner.domain.usecase.UserUC
import miner.respondError
import miner.respondFail
import miner.respondSuccess

@Location("/mine")
class MineAPILocation

class NotLoggedInException : Exception()

data class MinePositionParams(val x: Int, val y: Int)

fun Route.mine(userUC: UserUC, miningUC: MiningUC) {
    post<MineAPILocation> {
        try {
            // TODO: Remove cookie dependency
            val loggedInUser = call.sessions.get<MinerSession>()?.let { userUC.getUserById(it.userId) } ?: throw NotLoggedInException()

            val (x: Int, y: Int)= call.receive(MinePositionParams::class)

            val results = miningUC.mine(loggedInUser.id, x, y)
            if (results != null) {
                call.respondSuccess(results)
            } else {
                call.respond(HttpStatusCode.Accepted)
            }
        } catch (e: NotLoggedInException) {
            call.respondFail(HttpStatusCode.Unauthorized)
        } catch (e: ContentTransformationException) {
            call.respondFail(HttpStatusCode.BadRequest, "Missing or invalid parameters.")
        } catch (e: Exception) {
            call.respondError(HttpStatusCode.InternalServerError, e.message.orEmpty())
        }
    }
}
