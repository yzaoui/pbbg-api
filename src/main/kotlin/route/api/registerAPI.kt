package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.usecase.CredentialsFormatException
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.domain.usecase.UsernameNotAvailableException
import com.bitwiserain.pbbg.makeToken
import com.bitwiserain.pbbg.respondFail
import com.bitwiserain.pbbg.respondSuccess
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.Route
import io.ktor.routing.post

fun Route.registerAPI(userUC: UserUC) = post("/register") {
    try {
        val params = call.receive<Map<String, Any>>()
        val usernameParam = params["username"] as? String
        val passwordParam = params["password"] as? String

        if (usernameParam == null || passwordParam == null) {
            return@post call.respondFail(mapOf(
                "username" to if (usernameParam == null) "A username is required." else null,
                "password" to if (passwordParam == null) "A password is required." else null
            ))
        }

        val token = userUC.registerUser(usernameParam, passwordParam)
            .let { application.makeToken(it) }

        call.respondSuccess(mapOf("token" to token))
    } catch (e: UsernameNotAvailableException) {
        call.respondFail(mapOf("username" to "The username \"${e.username}\" is unavailable."))
    } catch (e: CredentialsFormatException) {
        call.respondFail(mapOf("username" to e.usernameError, "password" to e.passwordError))
    }
}
