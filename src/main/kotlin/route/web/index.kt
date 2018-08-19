package route.web

import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import miner.MinerSession
import miner.domain.usecase.UserUC
import miner.href
import miner.route.web.EquipmentLocation
import miner.view.homeGuestPage
import miner.view.homeMemberPage

@Location("/")
class IndexLocation

fun Route.index(userUC: UserUC) {
    get<IndexLocation> { _ ->
        val loggedInUser = call.sessions.get<MinerSession>()?.let { userUC.getUserById(it.userId) }

        if (loggedInUser != null) {
            call.respondHtmlTemplate(
                homeMemberPage(
                    mineURL = href(MineWebLocation()),
                    equipmentURL = href(EquipmentLocation()),
                    logoutURL = href(LogoutLocation())
                )
            ) {}
        } else {
            call.respondHtmlTemplate(
                homeGuestPage(
                    registerURL = href(RegisterLocation()),
                    loginURL = href(LoginLocation())
                )
            ) {}
        }
    }
}
