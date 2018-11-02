package com.bitwiserain.pbbg.route.web

import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.interceptSetUserOrRedirect
import com.bitwiserain.pbbg.memberPageVM
import com.bitwiserain.pbbg.view.page.squadPage
import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.locations.Location
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

const val SQUAD_PATH = "/squad"
@Location(SQUAD_PATH)
class SquadLocation

fun Route.squadWeb(userUC: UserUC) = route(SQUAD_PATH) {
    interceptSetUserOrRedirect(userUC)

    get {
        call.respondHtmlTemplate(squadPage(memberPageVM = call.attributes[memberPageVM])) {}
    }
}
