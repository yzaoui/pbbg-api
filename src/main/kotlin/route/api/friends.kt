package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.model.friends.FriendInfo
import com.bitwiserain.pbbg.domain.model.friends.Friends
import com.bitwiserain.pbbg.domain.usecase.*
import com.bitwiserain.pbbg.respondFail
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.user
import com.bitwiserain.pbbg.view.model.friends.FriendInfoJSON
import com.bitwiserain.pbbg.view.model.friends.FriendsJSON
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.*

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
