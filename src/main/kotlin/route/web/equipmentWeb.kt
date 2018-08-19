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
import miner.view.equipmentPage
import route.web.IndexLocation

@Location("/equipment")
class EquipmentLocation

fun Route.equipmentWeb(userUC: UserUC) = route("/equipment") {
    interceptSetUserOrRedirect(userUC)

    get {
        call.respondHtmlTemplate(equipmentPage(href(IndexLocation()))) {}
    }
}
