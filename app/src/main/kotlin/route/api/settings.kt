package com.bitwiserain.pbbg.app.route.api

import com.bitwiserain.pbbg.app.domain.usecase.ChangePasswordUC
import com.bitwiserain.pbbg.app.respondFail
import com.bitwiserain.pbbg.app.respondSuccess
import com.bitwiserain.pbbg.app.user
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

fun Route.settings(changePassword: ChangePasswordUC) = route("/settings") {
    post("/change-password") {
        val params = call.receive<JsonObject>()

        val currentPassword = params["currentPassword"]?.let { it.jsonPrimitive.content }
        val newPassword = params["newPassword"]?.let { it.jsonPrimitive.content }
        val confirmNewPassword = params["confirmNewPassword"]?.let { it.jsonPrimitive.content }

        if (currentPassword == null || newPassword == null || confirmNewPassword == null) {
            return@post call.respondFail("Missing parameter(s).")
        }

        val result = changePassword(call.user.id, currentPassword, newPassword, confirmNewPassword)

        when (result) {
            ChangePasswordUC.Result.Success -> {
                call.respondSuccess("Password successfully changed.")
            }
            ChangePasswordUC.Result.WrongCurrentPasswordError -> {
                call.respondFail("Wrong current password.")
            }
            ChangePasswordUC.Result.UnconfirmedNewPasswordError -> {
                call.respondFail("New password and confirmation do not match.")
            }
            ChangePasswordUC.Result.NewPasswordNotNewError -> {
                call.respondFail("New password is the same as current password.")
            }
            ChangePasswordUC.Result.IllegalPasswordError -> {
                call.respondFail("New password does not fit requirements.")
            }
        }
    }
}
