package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.usecase.LoginUC
import com.bitwiserain.pbbg.makeToken
import com.bitwiserain.pbbg.respondFail
import com.bitwiserain.pbbg.respondSuccess
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.Route
import io.ktor.routing.post

fun Route.loginAPI(login: LoginUC) = post("/login") {
    val params = call.receive<Map<String, Any>>()

    val errors = mutableMapOf<String, String>()

    val usernameParam = params["username"]
    val usernameError = if (usernameParam == null || usernameParam !is String) {
        "A username is required"
    } else {
        null
    }

    val passwordParam = params["password"]
    val passwordError = if (passwordParam == null || passwordParam !is String) {
        "A password is required"
    } else {
        null
    }

    usernameError?.let { errors["username"] = it }
    passwordError?.let { errors["password"] = it }

    if (errors.isNotEmpty()) return@post call.respondFail(errors)

    val result = login(username = usernameParam as String, password = passwordParam as String)

    when (result) {
        is LoginUC.Result.Success -> {
            call.respondSuccess(mapOf("token" to application.makeToken(result.userId)))
        }
        LoginUC.Result.CredentialsDontMatchError -> {
            call.respondFail("Credentials do not match an existing account")
        }
    }
}
