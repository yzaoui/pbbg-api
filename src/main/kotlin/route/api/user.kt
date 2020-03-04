package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.model.UserProfile
import com.bitwiserain.pbbg.domain.usecase.TargetUserDoesNotExistException
import com.bitwiserain.pbbg.domain.usecase.UserProfileUC
import com.bitwiserain.pbbg.respondFail
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.userOptional
import com.bitwiserain.pbbg.view.model.UserProfileJSON
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

private const val USER_ID_PARAM = "id"

fun Route.user(userProfileUC: UserProfileUC) = route("/user/{$USER_ID_PARAM}") {
    get {
        val targetUserId = call.parameters[USER_ID_PARAM]?.toIntOrNull() ?: return@get call.respondFail()

        try {
            val profile = userProfileUC.getUserProfile(targetUserId, call.userOptional)

            call.respondSuccess(profile.toJSON())
        } catch (e: TargetUserDoesNotExistException) {
            call.respondFail()
        }
    }
}

private fun UserProfile.toJSON() = UserProfileJSON(
    username = username,
    friends = friends
)
