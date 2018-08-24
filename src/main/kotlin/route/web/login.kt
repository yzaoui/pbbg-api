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
import miner.view.loginPage

@Location("/login")
class LoginLocation

fun Route.login(userUC: UserUC) = route("/login") {
    interceptGuestOnly(userUC)

    get {
        call.respondHtmlTemplate(
            loginPage(
                loginURL = href(LoginLocation()),
                homeURL = href(IndexLocation()),
                registerURL = href(RegisterLocation()),
                guestPageVM = GuestPageVM()
            )
        ) {}
    }

    post {
        val params = call.receiveParameters()

        val usernameParam = params["username"]
        val passwordParam = params["password"]

        if (usernameParam != null && passwordParam != null) {
            val user = userUC.getUserByUsername(usernameParam)

            if (user != null && BCrypt.verifyer().verify(passwordParam.toByteArray(), user.passwordHash).verified) {
                call.sessions.set(ApplicationSession(user.id))
                call.respondRedirect(href(IndexLocation()))
            } else {
                // TODO: Add errors here
                call.respondRedirect(href(LoginLocation()))
            }
        }
    }
}
