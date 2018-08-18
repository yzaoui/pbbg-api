package route.web

import at.favre.lib.crypto.bcrypt.BCrypt
import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receiveParameters
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import miner.MinerSession
import miner.domain.usecase.UserUC
import miner.href
import miner.view.registerPage

@Location("/register")
class RegisterLocation

fun Route.register(userUC: UserUC) {
    get<RegisterLocation> {
        val loggedInUser = call.sessions.get<MinerSession>()?.let { userUC.getUserById(it.userId) }
        if (loggedInUser != null) {
            call.respondRedirect(href(IndexLocation()))
            return@get
        }

        call.respondHtmlTemplate(registerPage(registerURL = href(RegisterLocation()))) {}
    }

    post<RegisterLocation> {
        val params = call.receiveParameters()

        val usernameParam = params["username"]
        val passwordParam = params["password"]

        if (usernameParam != null && passwordParam != null) {
            val userId = userUC.registerUser(
                username = usernameParam,
                passwordHash = BCrypt.withDefaults().hash(12, passwordParam.toByteArray())
            )

            call.sessions.set(MinerSession(userId))
            call.respondRedirect(href(IndexLocation()))
        } else {
            // TODO: Add errors here
            call.respondRedirect(href(RegisterLocation()))
        }
    }
}
