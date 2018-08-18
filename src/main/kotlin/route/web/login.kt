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
import miner.domain.usecase.UserUseCase
import miner.href
import miner.view.loginPage

@Location("/login")
class LoginLocation

fun Route.login(userUC: UserUseCase) {
    get<LoginLocation> {
        val loggedInUser = call.sessions.get<MinerSession>()?.let { userUC.getUserById(it.userId) }
        if (loggedInUser != null) {
            call.respondRedirect(href(IndexLocation()))
            return@get
        }

        call.respondHtmlTemplate(
            loginPage(
                loginURL = href(LoginLocation())
            )
        ) {}
    }

    post<LoginLocation> {
        val params = call.receiveParameters()

        val usernameParam = params["username"]
        val passwordParam = params["password"]

        if (usernameParam != null && passwordParam != null) {
            val user = userUC.getUserByUsername(usernameParam)

            if (user != null && BCrypt.verifyer().verify(passwordParam.toByteArray(), user.passwordHash).verified) {
                call.sessions.set(MinerSession(user.id))
                call.respondRedirect(href(IndexLocation()))
            } else {
                // TODO: Add errors here
                call.respondRedirect(href(LoginLocation()))
            }
        }
    }
}
