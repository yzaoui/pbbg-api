package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.usecase.RegisterUserUC
import com.bitwiserain.pbbg.makeToken
import com.bitwiserain.pbbg.respondFail
import com.bitwiserain.pbbg.respondSuccess
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.Route
import io.ktor.routing.post

fun Route.registerAPI(registerUser: RegisterUserUC) = post("/register") {
    val params = call.receive<Map<String, Any>>()
    val usernameParam = params["username"] as? String
    val passwordParam = params["password"] as? String

    if (usernameParam == null || passwordParam == null) {
        return@post call.respondFail(mapOf(
            "username" to if (usernameParam == null) "A username is required." else null,
            "password" to if (passwordParam == null) "A password is required." else null
        ))
    }

    val result = registerUser(usernameParam, passwordParam)

    when (result) {
        is RegisterUserUC.Result.Success -> {
            call.respondSuccess(mapOf("token" to application.makeToken(result.userId)))
        }
        RegisterUserUC.Result.UsernameNotAvailableError -> {
            call.respondFail(mapOf("username" to "The username \"$usernameParam\" is unavailable."))
        }
        is RegisterUserUC.Result.CredentialsFormatError -> {
            call.respondFail(mapOf("username" to result.usernameError, "password" to result.passwordError))
        }
    }
}
