package com.bitwiserain.pbbg.route.web

import com.bitwiserain.pbbg.domain.usecase.IllegalPasswordException
import com.bitwiserain.pbbg.domain.usecase.UnconfirmedNewPasswordException
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.domain.usecase.WrongCurrentPasswordException
import com.bitwiserain.pbbg.href
import com.bitwiserain.pbbg.interceptSetUserOrRedirect
import com.bitwiserain.pbbg.loggedInUserKey
import com.bitwiserain.pbbg.memberPageVM
import com.bitwiserain.pbbg.view.page.settingsPage
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.html.respondHtmlTemplate
import io.ktor.locations.Location
import io.ktor.request.receiveParameters
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.pipeline.PipelineContext

const val CHANGE_PASSWORD_PATH = "/change-password"

@Location("/settings")
class SettingsLocation {
    @Location(CHANGE_PASSWORD_PATH)
    class ChangePasswordLocation
}

fun Route.settings(userUC: UserUC) = route("/settings") {
    interceptSetUserOrRedirect(userUC)

    get {
        respondSettingsPage()
    }

    route(CHANGE_PASSWORD_PATH) {
        post {
            try {
                val params = call.receiveParameters()
                val currentPassword: String? = params["currentPassword"]
                val newPassword: String? = params["newPassword"]
                val confirmNewPassword: String? = params["confirmNewPassword"]

                if (currentPassword == null || newPassword == null || confirmNewPassword == null) {
                    return@post respondSettingsPage(error = "Missing parameter(s).")
                }

                val loggedInUser = call.attributes[loggedInUserKey]

                userUC.changePassword(loggedInUser.id, currentPassword, newPassword, confirmNewPassword)

                call.respondRedirect(href(SettingsLocation()))
            } catch (e: WrongCurrentPasswordException) {
                respondSettingsPage(error = "Wrong current password.")
            } catch (e: UnconfirmedNewPasswordException) {
                respondSettingsPage(error = "New passwords do not match.")
            } catch (e: IllegalPasswordException) {
                respondSettingsPage(error = "New password does not fit requirements.")
            }
        }
    }
}

private suspend inline fun PipelineContext<Unit, ApplicationCall>.respondSettingsPage(error: String? = null) {
    call.respondHtmlTemplate(
        settingsPage(
            memberPageVM = call.attributes[memberPageVM],
            changePasswordUrl = href(SettingsLocation.ChangePasswordLocation()),
            error = error
        )
    ) {}
}
