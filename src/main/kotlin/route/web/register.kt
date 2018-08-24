package route.web

import at.favre.lib.crypto.bcrypt.BCrypt
import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.locations.Location
import io.ktor.request.receiveParameters
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import miner.ApplicationSession
import miner.domain.usecase.UserUC
import miner.href
import miner.interceptGuestOnly
import miner.view.GuestPageVM
import miner.view.registerPage

@Location("/register")
class RegisterLocation

fun Route.register(userUC: UserUC) = route("/register") {
    interceptGuestOnly(userUC)

    get {
        call.respondHtmlTemplate(registerPage(
            registerURL = href(RegisterLocation()),
            homeURL = href(IndexLocation()),
            loginURL = href(LoginLocation()),
            guestPageVM = GuestPageVM()
        )) {}
    }

    post {
        val params = call.receiveParameters()

        val usernameParam = params["username"]
        val passwordParam = params["password"]

        if (usernameParam != null && passwordParam != null) {
            val userId = userUC.registerUser(
                username = usernameParam,
                passwordHash = BCrypt.withDefaults().hash(12, passwordParam.toByteArray())
            )

            call.sessions.set(ApplicationSession(userId))
            call.respondRedirect(href(IndexLocation()))
        } else {
            // TODO: Add errors here
            call.respondRedirect(href(RegisterLocation()))
        }
    }
}
