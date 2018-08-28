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
import pbbg.view.equipmentPage

@Location("/equipment")
class EquipmentLocation

fun Route.equipmentWeb(userUC: UserUC) = route("/equipment") {
    interceptSetUserOrRedirect(userUC)

    get {
        call.respondHtmlTemplate(equipmentPage(call.attributes[memberPageVM])) {}
    }
}
