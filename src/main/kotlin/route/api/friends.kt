package com.bitwiserain.pbbg.route.api

import com.bitwiserain.pbbg.domain.model.friends.FriendInfo
import com.bitwiserain.pbbg.domain.model.friends.Friends
import com.bitwiserain.pbbg.domain.usecase.FriendsUC
import com.bitwiserain.pbbg.respondSuccess
import com.bitwiserain.pbbg.user
import com.bitwiserain.pbbg.view.model.friends.FriendInfoJSON
import com.bitwiserain.pbbg.view.model.friends.FriendsJSON
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route

fun Route.friends(friendsUC: FriendsUC) = route("/friends") {
    get {
        val friends = friendsUC.getFriends(call.user.id)

        call.respondSuccess(friends.toJSON())
    }
}

private fun Friends.toJSON() = FriendsJSON(
    friendInfos = friendInfos.map { it.toJSON() }
)

private fun FriendInfo.toJSON() = FriendInfoJSON(
    userId = userId,
    username = username,
    friendship = friendship.toJSON()
)
