package com.bitwiserain.pbbg.app.db.usecase

import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.FriendsTable
import com.bitwiserain.pbbg.app.db.repository.UserTable
import com.bitwiserain.pbbg.app.domain.model.friends.FriendInfo
import com.bitwiserain.pbbg.app.domain.model.friends.Friends
import com.bitwiserain.pbbg.app.domain.model.friends.Friendship
import com.bitwiserain.pbbg.app.domain.usecase.FriendsUC
import com.bitwiserain.pbbg.app.domain.usecase.FriendshipNotConfirmedException
import com.bitwiserain.pbbg.app.domain.usecase.FriendshipNotNoneException
import com.bitwiserain.pbbg.app.domain.usecase.FriendshipNotRequestReceivedException
import com.bitwiserain.pbbg.app.domain.usecase.FriendshipNotRequestSentException
import com.bitwiserain.pbbg.app.domain.usecase.SelfFriendshipException
import com.bitwiserain.pbbg.app.domain.usecase.TargetUserDoesNotExistException

class FriendsUCImpl(private val transaction: Transaction, private val friendsTable: FriendsTable, private val userTable: UserTable) : FriendsUC {

    override fun getFriends(userId: Int): Friends = transaction {
        // TODO: Could combine these into a single query
        val friendships = friendsTable.getFriends(userId)
        val users = userTable.getUsersById(friendships.map { it.userId })

        Friends(friendships.map { FriendInfo(it.userId, users.getValue(it.userId).username, it.friendship) })
    }

    override fun addFriend(currentUserId: Int, targetUserId: Int): Friendship = transaction {
        if (currentUserId == targetUserId) throw SelfFriendshipException
        if (!userTable.userExists(targetUserId)) throw TargetUserDoesNotExistException
        if (friendsTable.getFriendship(currentUserId, targetUserId) != Friendship.NONE) throw FriendshipNotNoneException

        friendsTable.insertRequest(currentUserId, targetUserId)

        return@transaction Friendship.REQUEST_SENT
    }

    override fun removeFriend(currentUserId: Int, targetUserId: Int): Friendship = transaction {
        if (currentUserId == targetUserId) throw SelfFriendshipException
        if (!userTable.userExists(targetUserId)) throw TargetUserDoesNotExistException
        if (friendsTable.getFriendship(currentUserId, targetUserId) != Friendship.CONFIRMED) throw FriendshipNotConfirmedException

        friendsTable.deleteFriendship(currentUserId, targetUserId)

        return@transaction Friendship.NONE
    }

    override fun acceptRequest(currentUserId: Int, targetUserId: Int): Friendship = transaction {
        if (currentUserId == targetUserId) throw SelfFriendshipException
        if (!userTable.userExists(targetUserId)) throw TargetUserDoesNotExistException
        if (friendsTable.getFriendship(currentUserId, targetUserId) != Friendship.REQUEST_RECEIVED) throw FriendshipNotRequestReceivedException

        friendsTable.confirmRequest(currentUserId, targetUserId)

        return@transaction Friendship.CONFIRMED
    }

    override fun cancelRequest(currentUserId: Int, targetUserId: Int): Friendship = transaction {
        if (currentUserId == targetUserId) throw SelfFriendshipException
        if (!userTable.userExists(targetUserId)) throw TargetUserDoesNotExistException
        if (friendsTable.getFriendship(currentUserId, targetUserId) != Friendship.REQUEST_SENT) throw FriendshipNotRequestSentException

        friendsTable.cancelRequest(currentUserId, targetUserId)

        return@transaction Friendship.NONE
    }
}
