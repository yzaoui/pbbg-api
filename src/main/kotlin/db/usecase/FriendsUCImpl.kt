package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.FriendsTable
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.model.friends.FriendInfo
import com.bitwiserain.pbbg.domain.model.friends.Friends
import com.bitwiserain.pbbg.domain.model.friends.Friendship
import com.bitwiserain.pbbg.domain.usecase.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class FriendsUCImpl(private val db: Database) : FriendsUC {
    override fun getFriends(userId: Int): Friends = transaction(db) {
        // TODO: Could combine these into a single query
        val friendships = FriendsTable.getFriends(userId)
        val users = UserTable.getUsersById(friendships.map { it.userId })

        Friends(friendships.map { FriendInfo(it.userId, users.getValue(it.userId).username, it.friendship) })
    }

    override fun addFriend(currentUserId: Int, targetUserId: Int): Friendship = transaction(db) {
        if (currentUserId == targetUserId) throw SelfFriendshipException
        if (!UserTable.userExists(targetUserId)) throw TargetUserDoesNotExistException
        if (FriendsTable.getFriendship(currentUserId, targetUserId) != Friendship.NONE) throw FriendshipNotNoneException

        FriendsTable.insertRequest(currentUserId, targetUserId)

        return@transaction Friendship.REQUEST_SENT
    }

    override fun removeFriend(currentUserId: Int, targetUserId: Int): Friendship = transaction(db) {
        if (currentUserId == targetUserId) throw SelfFriendshipException
        if (!UserTable.userExists(targetUserId)) throw TargetUserDoesNotExistException
        if (FriendsTable.getFriendship(currentUserId, targetUserId) != Friendship.CONFIRMED) throw FriendshipNotConfirmedException

        FriendsTable.deleteFriendship(currentUserId, targetUserId)

        return@transaction Friendship.NONE
    }

    override fun acceptRequest(currentUserId: Int, targetUserId: Int): Friendship = transaction(db) {
        if (currentUserId == targetUserId) throw SelfFriendshipException
        if (!UserTable.userExists(targetUserId)) throw TargetUserDoesNotExistException
        if (FriendsTable.getFriendship(currentUserId, targetUserId) != Friendship.REQUEST_RECEIVED) throw FriendshipNotRequestReceivedException

        FriendsTable.confirmRequest(currentUserId, targetUserId)

        return@transaction Friendship.CONFIRMED
    }

    override fun cancelRequest(currentUserId: Int, targetUserId: Int): Friendship = transaction(db) {
        if (currentUserId == targetUserId) throw SelfFriendshipException
        if (!UserTable.userExists(targetUserId)) throw TargetUserDoesNotExistException
        if (FriendsTable.getFriendship(currentUserId, targetUserId) != Friendship.REQUEST_SENT) throw FriendshipNotRequestSentException

        FriendsTable.cancelRequest(currentUserId, targetUserId)

        return@transaction Friendship.NONE
    }
}
