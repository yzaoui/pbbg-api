package com.bitwiserain.pbbg.app.route.api

import com.bitwiserain.pbbg.app.domain.model.friends.FriendInfo
import com.bitwiserain.pbbg.app.domain.model.friends.Friends
import com.bitwiserain.pbbg.app.domain.usecase.FriendsUC
import com.bitwiserain.pbbg.app.domain.usecase.FriendshipNotConfirmedException
import com.bitwiserain.pbbg.app.domain.usecase.FriendshipNotNoneException
import com.bitwiserain.pbbg.app.domain.usecase.FriendshipNotRequestReceivedException
import com.bitwiserain.pbbg.app.domain.usecase.FriendshipNotRequestSentException
import com.bitwiserain.pbbg.app.domain.usecase.SelfFriendshipException
import com.bitwiserain.pbbg.app.domain.usecase.TargetUserDoesNotExistException
import com.bitwiserain.pbbg.app.respondFail
import com.bitwiserain.pbbg.app.respondSuccess
import com.bitwiserain.pbbg.app.user
import com.bitwiserain.pbbg.app.view.model.friends.FriendInfoJSON
import com.bitwiserain.pbbg.app.view.model.friends.FriendsJSON
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.param
import io.ktor.routing.post
import io.ktor.routing.route

fun Route.friends(friendsUC: FriendsUC) = route("/friends") {
    get {
        val friends = friendsUC.getFriends(call.user.id)

        call.respondSuccess(friends.toJSON())
    }

    route("/change-friendship") {
        param("action") {
            post {
                try {
                    val action = call.request.queryParameters["action"]

                    if (action !in setOf("add-friend", "remove-friend", "accept-request", "cancel-request")) return@post call.respondFail()

                    val body = call.receive<ChangeFriendshipParams>()

                    val updatedFriendship = when (action) {
                        "add-friend" -> friendsUC.addFriend(call.user.id, body.userId)
                        "remove-friend" -> friendsUC.removeFriend(call.user.id, body.userId)
                        "accept-request" -> friendsUC.acceptRequest(call.user.id, body.userId)
                        "cancel-request" -> friendsUC.cancelRequest(call.user.id, body.userId)
                        else -> throw IllegalStateException()
                    }

                    call.respondSuccess(updatedFriendship.toJSON())
                } catch (e: SelfFriendshipException) {
                    call.respondFail("Cannot request friendship with yourself.")
                } catch (e: TargetUserDoesNotExistException) {
                    call.respondFail("Target user does not exist.")
                } catch (e: FriendshipNotNoneException) {
                    call.respondFail("Friendship is not uninitiated.")
                } catch (e: FriendshipNotConfirmedException) {
                    call.respondFail("Friendship is not confirmed.")
                } catch (e: FriendshipNotRequestReceivedException) {
                    call.respondFail("Friendship request not pending your acceptance.")
                } catch (e: FriendshipNotRequestSentException) {
                    call.respondFail("Friendship request not pending target user's acceptance.")
                }
            }
        }
    }
}

private data class ChangeFriendshipParams(val userId: Int)

private fun Friends.toJSON() = FriendsJSON(
    friendInfos = friendInfos.map { it.toJSON() }
)

fun FriendInfo.toJSON() = FriendInfoJSON(
    userId = userId,
    username = username,
    friendship = friendship.toJSON()
)
