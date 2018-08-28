package route.web

import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.locations.Location
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import pbbg.domain.usecase.MiningUC
import pbbg.domain.usecase.UserUC
import pbbg.href
import pbbg.interceptSetUserOrRedirect
import pbbg.loggedInUserKey
import pbbg.memberPageVM
import pbbg.view.minePage

@Location("/mine")
class MineWebLocation

fun Route.mineWeb(userUC: UserUC, miningUC: MiningUC) = route("/mine") {
    interceptSetUserOrRedirect(userUC)

    get {
        call.respondHtmlTemplate(
            minePage(
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
