package route.web

import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.locations.Location
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import miner.domain.usecase.MiningUC
import miner.domain.usecase.UserUC
import miner.href
import miner.interceptSetUserOrRedirect
import miner.loggedInUserKey
import miner.memberPageVM
import miner.view.minePage

@Location("/mine")
class MineWebLocation

fun Route.mineWeb(userUC: UserUC, miningUC: MiningUC) = route("/mine") {
    interceptSetUserOrRedirect(userUC)

    get {
        call.respondHtmlTemplate(
            minePage(
                homeURL = href(IndexLocation()),
                memberPageVM = call.attributes[memberPageVM]
            )
        ) {}
    }

    post {
        // TODO: Remove this, move to API
        val loggedInUser = call.attributes[loggedInUserKey]

        miningUC.generateMine(loggedInUser.id, 30, 20)

        call.respondRedirect(href(MineWebLocation()))
    }
}
