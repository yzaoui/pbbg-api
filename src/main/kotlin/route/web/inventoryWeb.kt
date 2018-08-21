package miner.route.web

import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.locations.Location
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import miner.domain.usecase.UserUC
import miner.href
import miner.interceptSetUserOrRedirect
import miner.view.inventoryPage
import route.web.IndexLocation

@Location("/inventory")
class InventoryLocation

fun Route.inventoryWeb(userUC: UserUC) = route("/inventory") {
    interceptSetUserOrRedirect(userUC)

    get {
        call.respondHtmlTemplate(inventoryPage(href(IndexLocation()))) {}
    }
}
