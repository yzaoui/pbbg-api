package route.web

import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.locations.Location
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import pbbg.domain.usecase.UserUC
import pbbg.getMemberPageVM
import pbbg.getUserUsingSession
import pbbg.href
import pbbg.route.web.EquipmentLocation
import pbbg.route.web.InventoryLocation
import pbbg.view.GuestPageVM
import pbbg.view.homeGuestPage
import pbbg.view.homeMemberPage

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
                    memberPageVM = getMemberPageVM(loggedInUser)
                )
            ) {}
        } else {
            call.respondHtmlTemplate(
                homeGuestPage(
                    registerURL = href(RegisterLocation()),
                    loginURL = href(LoginLocation()),
                    guestPageVM = GuestPageVM()
                )
            ) {}
        }
    }
}
