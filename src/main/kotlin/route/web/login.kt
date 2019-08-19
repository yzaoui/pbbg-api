package com.bitwiserain.pbbg.route.web

import com.bitwiserain.pbbg.ApplicationSession
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.getGuestPageVM
import com.bitwiserain.pbbg.href
import com.bitwiserain.pbbg.interceptGuestOnly
import com.bitwiserain.pbbg.view.page.loginPage
import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.http.HttpStatusCode
import io.ktor.locations.Location
import io.ktor.request.receiveParameters
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.sessions.sessions
import io.ktor.sessions.set

@Location("/login")
class LoginLocation

fun Route.login(userUC: UserUC) = route("/login") {
    interceptGuestOnly(userUC)

    get {
        call.respondHtmlTemplate(
            loginPage(
                loginURL = href(LoginLocation()),
                registerURL = href(RegisterLocation()),
                guestPageVM = getGuestPageVM()
            )
        ) {}
    }

    post {
        val params = call.receiveParameters()

        val usernameParam = params["username"]
        val passwordParam = params["password"]

        if (usernameParam == null || passwordParam == null) {
            return@post call.respondHtmlTemplate(loginPage(
                loginURL = href(LoginLocation()),
                registerURL = href(RegisterLocation()),
                guestPageVM = getGuestPageVM(),
                error = "Missing parameter(s)."
            )) {}
        }

        // TODO: Consider adding artificial delay to hide user's existence by simulating hashing time
        val userId = userUC.getUserIdByCredentials(usernameParam, passwordParam)

        if (userId != null) {
            call.sessions.set(ApplicationSession(userId))
            call.respondRedirect(href(IndexLocation()))
        } else {
            // TODO: Consider using PRG pattern to get rid of refresh-resubmitting-POST issue
            call.respondHtmlTemplate(
                template = loginPage(
                    loginURL = href(LoginLocation()),
                    registerURL = href(RegisterLocation()),
                    guestPageVM = getGuestPageVM(),
                    error = "Credentials do not match an existing account."
                ),
                status = HttpStatusCode.BadRequest
            ) {}
        }
    }
}
