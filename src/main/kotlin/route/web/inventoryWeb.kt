package pbbg.route.web

import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.locations.Location
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import pbbg.domain.usecase.UserUC
import pbbg.interceptSetUserOrRedirect
import pbbg.memberPageVM
import pbbg.view.inventoryPage

@Location("/inventory")
class InventoryLocation

fun Route.inventoryWeb(userUC: UserUC) = route("/inventory") {
    interceptSetUserOrRedirect(userUC)

    get {
        call.respondHtmlTemplate(
            inventoryPage(
                memberPageVM = call.attributes[memberPageVM]
            )
        ) {}
    }
}
