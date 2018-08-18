package route.web

import io.ktor.application.call
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.sessions.clear
import io.ktor.sessions.sessions
import miner.MinerSession
import miner.href

@Location("/logout")
class LogoutLocation

fun Route.logout() {
    post<LogoutLocation> {
        call.sessions.clear<MinerSession>()
        call.respondRedirect(href(IndexLocation()))
    }
}
