package miner.route

import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import miner.MinerSession
import miner.domain.usecase.UserUseCase
import miner.href
import miner.view.MainTemplate

@Location("/register")
class RegisterLocation

fun Route.register(userUC: UserUseCase) {
    get<RegisterLocation> {
        val loggedInUser = call.sessions.get<MinerSession>()?.let { userUC.getUserById(it.userId) }
        if (loggedInUser != null) {
            call.respondRedirect(href(IndexLocation()))
            return@get
        }

        call.respondHtmlTemplate(MainTemplate()) {}
    }
}
