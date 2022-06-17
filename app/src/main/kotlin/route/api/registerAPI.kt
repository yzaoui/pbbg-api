package com.bitwiserain.pbbg.app.route.api

import com.bitwiserain.pbbg.app.domain.usecase.RegisterUserUC
import com.bitwiserain.pbbg.app.makeToken
import com.bitwiserain.pbbg.app.respondFail
import com.bitwiserain.pbbg.app.respondSuccess
import io.ktor.server.application.application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

fun Route.registerAPI(registerUser: RegisterUserUC) = post("/register") {
    val params = call.receive<JsonObject>()
    val usernameParam = params["username"]?.let { it.jsonPrimitive.content }
    val passwordParam = params["password"]?.let { it.jsonPrimitive.content }

    if (usernameParam == null || passwordParam == null) {
        return@post call.respondFail(buildMap {
            if (usernameParam == null) put("username", "A username is required.")
            if (passwordParam == null) put("password", "A password is required.")
        })
    }

    val result = registerUser(username = usernameParam, password = passwordParam)

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
