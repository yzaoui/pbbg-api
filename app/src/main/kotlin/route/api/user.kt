package com.bitwiserain.pbbg.app.route.api

import com.bitwiserain.pbbg.app.domain.model.UserProfile
import com.bitwiserain.pbbg.app.domain.model.friends.Friendship
import com.bitwiserain.pbbg.app.domain.usecase.TargetUserDoesNotExistException
import com.bitwiserain.pbbg.app.domain.usecase.UserProfileUC
import com.bitwiserain.pbbg.app.respondFail
import com.bitwiserain.pbbg.app.respondSuccess
import com.bitwiserain.pbbg.app.user
import com.bitwiserain.pbbg.app.userOptional
import com.bitwiserain.pbbg.app.view.model.UserProfileJSON
import com.bitwiserain.pbbg.app.view.model.friends.FriendshipJSON
import io.ktor.server.application.call
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.param
import io.ktor.server.routing.route

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
    joinedDate = joinedInstant.toString(),
    friendship = friendship?.toJSON()
)

fun Friendship.toJSON() = when (this) {
    Friendship.NONE -> FriendshipJSON.NONE
    Friendship.REQUEST_SENT -> FriendshipJSON.REQUEST_SENT
    Friendship.REQUEST_RECEIVED -> FriendshipJSON.REQUEST_RECEIVED
    Friendship.CONFIRMED -> FriendshipJSON.CONFIRMED
}
