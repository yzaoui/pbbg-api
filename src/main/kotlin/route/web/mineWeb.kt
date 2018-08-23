package route.web

import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.locations.Location
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import miner.data.model.Mine
import miner.data.model.MineEntity
import miner.domain.usecase.MiningUC
import miner.domain.usecase.UserUC
import miner.href
import miner.interceptSetUserOrRedirect
import miner.loggedInUserKey
import miner.view.minePage
import miner.view.minePageExistingMine
import miner.view.minePageNoMine
import miner.view.model.MineItemVM
import miner.view.model.MineVM

@Location("/mine")
class MineWebLocation

fun Route.mineWeb(userUC: UserUC, miningUC: MiningUC) = route("/mine") {
    interceptSetUserOrRedirect(userUC)

    get {
        call.respondHtmlTemplate(minePage(href(IndexLocation()))) {}
    }

    post {
        // TODO: Remove this, move to API
        val loggedInUser = call.attributes[loggedInUserKey]

        miningUC.generateMine(loggedInUser.id, 30, 20)

        call.respondRedirect(href(MineWebLocation()))
    }
}
