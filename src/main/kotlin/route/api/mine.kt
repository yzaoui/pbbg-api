package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.*
import com.bitwiserain.pbbg.domain.model.mine.Mine
import com.bitwiserain.pbbg.domain.model.mine.MineEntity
import com.bitwiserain.pbbg.domain.usecase.MiningUC
import com.bitwiserain.pbbg.domain.usecase.NoEquippedPickaxeException
import com.bitwiserain.pbbg.domain.usecase.NotInMineSessionException
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.view.model.mine.MineEntityJSON
import com.bitwiserain.pbbg.view.model.mine.MineJSON
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.request.ContentTransformationException
import io.ktor.request.receive
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

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

            val items = miningUC.submitMineAction(loggedInUser.id, x, y).map { it.item.toJSON() } // TODO: Show exp as well

            call.respondSuccess(items)
        } catch (e: ContentTransformationException) {
            call.respondFail(HttpStatusCode.BadRequest, "Missing or invalid parameters.")
        } catch (e: NoEquippedPickaxeException) {
            call.respondFail(HttpStatusCode.Forbidden, "A pickaxe must be equipped in order to perform a mining operation.")
        } catch (e: NotInMineSessionException) {
            call.respondFail(HttpStatusCode.Forbidden, "A mine session must be in progress in order to perform a mining operation.")
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

// TODO: Find appropriate place for this adapter
private fun Mine.toVM() = MineJSON(
    width = width,
    height = height,
    cells = List(height) { y -> List(width) { x -> grid[x to y]?.toVM() } }
)

// TODO: Find appropriate place for this adapter
private fun MineEntity.toVM() = MineEntityJSON(
    imageURL = when (this) {
        MineEntity.ROCK -> "/img/mine/rock.png"
        MineEntity.COAL -> "/img/mine/coal.png"
    }
)
