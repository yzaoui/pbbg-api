package miner.route.api

import data.model.MineResultItem
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.request.ContentTransformationException
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import miner.*
import miner.data.model.Mine
import miner.data.model.MineEntity
import miner.domain.usecase.MiningUC
import miner.domain.usecase.UserUC
import miner.view.model.MineItemVM
import miner.view.model.MineVM

@Location("/mine")
class MineAPILocation

data class MinePositionParams(val x: Int, val y: Int)

fun Route.mine(userUC: UserUC, miningUC: MiningUC) = route("/mine") {
    interceptSetUserOr401(userUC)

    get {
        val loggedInUser = call.attributes[loggedInUserKey]
        val mine = miningUC.getMine(loggedInUser.id)

        call.respondSuccess(mine?.toVM())
    }

    post {
        try {
            // TODO: Remove cookie dependency
            val loggedInUser = call.attributes[loggedInUserKey]

            val (x: Int, y: Int)= call.receive(MinePositionParams::class)

            val results = miningUC.mine(loggedInUser.id, x, y)
            if (results != null) {
                call.respondSuccess(MineResultItemsJSON(results))
            } else {
                call.respond(HttpStatusCode.Accepted)
            }
        } catch (e: ContentTransformationException) {
            call.respondFail(HttpStatusCode.BadRequest, "Missing or invalid parameters.")
        } catch (e: Exception) {
            call.respondError(HttpStatusCode.InternalServerError, e.message.orEmpty())
        }
    }

    route("/generate") {
        post {
            val loggedInUser = call.attributes[loggedInUserKey]

            val mine = miningUC.generateMine(loggedInUser.id, 30, 20)

            call.respondSuccess(mine.toVM())
        }
    }
}

data class MineResultItemsJSON(val results: List<MineResultItem>)

private fun Mine.toVM() = MineVM(
    width = width,
    height = height,
    cells = List(height) { y -> List(width) { x -> grid[x to y]?.toVM() } }
)

private fun MineEntity.toVM() = MineItemVM(
    imageURL = when (this) {
        MineEntity.ROCK -> "/img/mine/rock.png"
    }
)
