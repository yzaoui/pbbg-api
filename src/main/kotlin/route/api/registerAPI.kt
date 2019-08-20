package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.*
import com.bitwiserain.pbbg.domain.usecase.UserUC
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.routing.Route
import io.ktor.routing.post

fun Route.registerAPI(userUC: UserUC) = post("/register") {
    val params = call.receive<Map<String, Any>>()

    val errors = mutableMapOf<String, String>()

    val usernameParam = params["username"]
    val usernameError = if (usernameParam == null || usernameParam !is String) {
        "A username is required"
    } else if (!usernameParam.matches(USERNAME_REGEX.toRegex())) {
        USERNAME_REGEX_DESCRIPTION
    } else if (!userUC.usernameAvailable(usernameParam)) {
        "This username is unavailable"
    } else {
        null
    }

    val passwordParam = params["password"]
    val passwordError = if (passwordParam == null || passwordParam !is String) {
        "A password is required"
    } else if (!passwordParam.matches(PASSWORD_REGEX.toRegex())) {
        PASSWORD_REGEX_DESCRIPTION
    } else {
        null
    }

    usernameError?.let { errors["username"] = it }
    passwordError?.let { errors["password"] = it }

    if (errors.isEmpty()) {
        val userId = userUC.registerUser(
            username = usernameParam as String,
            password = passwordParam as String
        )

        call.respondSuccess(mapOf(
            "token" to application.makeToken(userId)
        ))
    } else {
        call.respondFail(HttpStatusCode.BadRequest, errors)
    }
}
