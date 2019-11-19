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

fun Route.settings(userUC: UserUC) = route("/settings") {
    post("/change-password") {
        try {
            val loggedInUser = call.user

            val params = call.receive<Map<String, Any>>()

            val currentPassword = params["currentPassword"]
            val newPassword = params["newPassword"]
            val confirmNewPassword = params["confirmNewPassword"]

            if (currentPassword !is String || newPassword !is String || confirmNewPassword !is String) {
                return@post call.respondFail("Missing parameter(s).")
            }

            userUC.changePassword(loggedInUser.id, currentPassword, newPassword, confirmNewPassword)

            call.respondSuccess("Password successfully changed.")
        } catch (e: WrongCurrentPasswordException) {
            call.respondFail("Wrong current password.")
        } catch (e: UnconfirmedNewPasswordException) {
            call.respondFail("New password and confirmation do not match.")
        } catch (e: NewPasswordNotNewException) {
            call.respondFail("New password is the same as current password.")
        } catch (e: IllegalPasswordException) {
            call.respondFail("New password does not fit requirements.")
        }
    }
}
