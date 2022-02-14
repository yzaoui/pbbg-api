package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.usecase.*
import com.bitwiserain.pbbg.respondFail
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.user
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route

fun Route.settings(changePassword: ChangePasswordUC) = route("/settings") {
    post("/change-password") {
        val params = call.receive<Map<String, Any>>()

        val currentPassword = params["currentPassword"]
        val newPassword = params["newPassword"]
        val confirmNewPassword = params["confirmNewPassword"]

        if (currentPassword !is String || newPassword !is String || confirmNewPassword !is String) {
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
