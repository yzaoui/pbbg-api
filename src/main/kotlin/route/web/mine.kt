package route.web

import data.model.Item
import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import miner.MinerSession
import miner.domain.usecase.UserUseCase
import miner.href
import miner.view.minePage
import miner.view.model.ItemVM
import java.util.*

@Location("/mine")
class MineLocation

fun Route.mine(userUC: UserUseCase) {
    get<MineLocation> {
        val loggedInUser = call.sessions.get<MinerSession>()?.let { userUC.getUserById(it.userId) }
//        if (loggedInUser == null) {
//            call.respondRedirect(href(IndexLocation()))
//            return@get
//        }

        val grid = generateGrid().map { row ->
            row.map { item ->
                item?.let { ItemVM(when(it) {
                    Item.ROCK -> "/img/item/rock.png"
                }) }
            }
        }

        call.respondHtmlTemplate(minePage(href(IndexLocation()), grid)) {}
    }
}

private val random = Random()

private fun generateGrid(): Array<Array<Item?>> = Array(20) {
    Array(20) {
        val roll = random.nextFloat()
        when {
            roll <= 0.05 -> Item.ROCK
            else -> null
        }
    }
}
