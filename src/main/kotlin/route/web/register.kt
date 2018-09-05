package com.bitwiserain.pbbg.route.web

import com.bitwiserain.pbbg.*
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.view.GuestPageVM
import com.bitwiserain.pbbg.view.registerPage
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

@Location("/register")
class RegisterLocation

fun Route.register(userUC: UserUC) = route("/register") {
    interceptGuestOnly(userUC)

    get {
        call.respondHtmlTemplate(registerPage(
            registerURL = href(RegisterLocation()),
            loginURL = href(LoginLocation()),
            guestPageVM = GuestPageVM()
        )) {}
    }

    post {
        val params = call.receiveParameters()

        val errors = mutableListOf<String>()

        val usernameParam = params["username"]
        if (usernameParam != null) {
            if (!usernameParam.matches(USERNAME_REGEX.toRegex())) errors.add(USERNAME_REGEX_DESCRIPTION)
            else if (!userUC.usernameAvailable(usernameParam)) errors.add("Username is unavailable")
        } else {
            errors.add("Username is missing.")
        }

        val passwordParam = params["password"]
        if (passwordParam != null) {
            if (!passwordParam.matches(PASSWORD_REGEX.toRegex())) errors.add(PASSWORD_REGEX_DESCRIPTION)
        } else {
            errors.add("Password is missing")
        }

        if (errors.isEmpty()) {
            // Relying on the fact that if either the username or password is null, the list of errors would not be empty
            val userId = userUC.registerUser(
                username = usernameParam!!,
                password = passwordParam!!
            )

            call.sessions.set(ApplicationSession(userId))
            call.respondRedirect(href(IndexLocation()))
        } else {
            // TODO: Consider using PRG pattern to get rid of refresh-resubmitting-POST issue
            call.respondHtmlTemplate(registerPage(
                registerURL = href(RegisterLocation()),
                loginURL = href(LoginLocation()),
                guestPageVM = GuestPageVM(),
                errors = errors
            )) {}
        }
    }
}
