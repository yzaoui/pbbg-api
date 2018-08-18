package route.web

import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import miner.MinerSession
import miner.data.model.Mine
import miner.data.model.MineItem
import miner.domain.usecase.MiningUC
import miner.domain.usecase.UserUC
import miner.href
import miner.view.minePageExistingMine
import miner.view.minePageNoMine
import miner.view.model.MineItemVM
import miner.view.model.MineVM

@Location("/mine")
class MineWebLocation

fun Route.mineWeb(userUC: UserUC, miningUC: MiningUC) {
    get<MineWebLocation> {
        val loggedInUser = call.sessions.get<MinerSession>()?.let { userUC.getUserById(it.userId) }
        if (loggedInUser == null) {
            call.respondRedirect(href(LoginLocation()))
            return@get
        }

        val mineSessionId = miningUC.getMineSession(userId = loggedInUser.id)

        if (mineSessionId != null) {
            val mine = miningUC.getMine(mineSessionId = mineSessionId)
            if (mine == null) {
                call.respondRedirect(href(MineWebLocation()))
                return@get
            }

            call.respondHtmlTemplate(minePageExistingMine(href(IndexLocation()), mine.toVM())) {}
        } else {
            call.respondHtmlTemplate(minePageNoMine(href(MineWebLocation()))) {}
        }
    }

    post<MineWebLocation> {
        val loggedInUser = call.sessions.get<MinerSession>()?.let { userUC.getUserById(it.userId) }
        if (loggedInUser == null) {
            call.respondRedirect(href(LoginLocation()))
            return@post
        }

        miningUC.generateMine(loggedInUser.id, 30, 20)

        call.respondRedirect(href(MineWebLocation()))
    }
}

private fun Mine.toVM() = MineVM(
    width = width,
    height = height,
    content = List(height) { y -> List(width) { x -> grid[x to y]?.toVM() } }
)

private fun MineItem.toVM() = MineItemVM(
    imageURL = when (this) {
        MineItem.ROCK -> "/img/item/rock.png"
    }
)
