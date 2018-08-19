package route.web

import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.sessions.clear
import io.ktor.sessions.sessions
import miner.ApplicationSession
import miner.href

@Location("/logout")
class LogoutLocation

fun Route.logout() = route("/logout") {
    post {
        call.sessions.clear<ApplicationSession>()
        call.respondRedirect(href(IndexLocation()))
    }
}
