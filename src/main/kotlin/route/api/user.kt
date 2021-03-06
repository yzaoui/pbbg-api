package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.model.UserProfile
import com.bitwiserain.pbbg.domain.model.friends.Friendship
import com.bitwiserain.pbbg.domain.usecase.TargetUserDoesNotExistException
import com.bitwiserain.pbbg.domain.usecase.UserProfileUC
import com.bitwiserain.pbbg.respondFail
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.user
import com.bitwiserain.pbbg.userOptional
import com.bitwiserain.pbbg.view.model.UserProfileJSON
import com.bitwiserain.pbbg.view.model.friends.FriendshipJSON
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.param
import io.ktor.routing.route

private const val USER_ID_PARAM = "id"

fun Route.user(userProfileUC: UserProfileUC) = route("/user") {
    get("/{$USER_ID_PARAM}") {
        val targetUserId = call.parameters[USER_ID_PARAM]?.toIntOrNull() ?: return@get call.respondFail()

        try {
            val profile = userProfileUC.getUserProfile(targetUserId, call.userOptional?.id)

            call.respondSuccess(profile.toJSON())
        } catch (e: TargetUserDoesNotExistException) {
            call.respondFail()
        }
    }

    route("/search") {
        param("text") {
            get {
                val text = call.request.queryParameters["text"] ?: return@get call.respondFail()

                val friendInfos = userProfileUC.searchUsers(call.user.id, text)

                call.respondSuccess(friendInfos.map { it.toJSON() })
            }
        }
    }
}

private fun UserProfile.toJSON() = UserProfileJSON(
    id = id,
    username = username,
    friendship = friendship?.toJSON()
)

fun Friendship.toJSON() = when (this) {
    Friendship.NONE -> FriendshipJSON.NONE
    Friendship.REQUEST_SENT -> FriendshipJSON.REQUEST_SENT
    Friendship.REQUEST_RECEIVED -> FriendshipJSON.REQUEST_RECEIVED
    Friendship.CONFIRMED -> FriendshipJSON.CONFIRMED
}
