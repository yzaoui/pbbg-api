package route.web

import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.locations.Location
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import miner.domain.usecase.UserUC
import miner.getUserUsingSession
import miner.href
import miner.route.web.EquipmentLocation
import miner.route.web.InventoryLocation
import miner.view.homeGuestPage
import miner.view.homeMemberPage

@Location("/")
class IndexLocation

fun Route.index(userUC: UserUC) = route("/") {
    get {
        val loggedInUser = getUserUsingSession(userUC)

        if (loggedInUser != null) {
            call.respondHtmlTemplate(
                homeMemberPage(
                    mineURL = href(MineWebLocation()),
                    inventoryURL = href(InventoryLocation()),
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
