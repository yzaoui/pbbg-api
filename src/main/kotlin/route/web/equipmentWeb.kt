package miner.route.web

import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import miner.MinerSession
import miner.domain.usecase.UserUC
import miner.href
import miner.view.equipmentPage
import route.web.IndexLocation
import route.web.LoginLocation

@Location("/equipment")
class EquipmentLocation

fun Route.equipmentWeb(userUC: UserUC) {
    get<EquipmentLocation> {
        val loggedInUser = call.sessions.get<MinerSession>()?.let { userUC.getUserById(it.userId) }
        if (loggedInUser == null) {
            call.respondRedirect(href(LoginLocation()))
            return@get
        }

        call.respondHtmlTemplate(equipmentPage(href(IndexLocation()))) {}
    }
}
