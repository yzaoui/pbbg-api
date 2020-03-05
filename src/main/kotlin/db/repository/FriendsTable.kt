package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.friends.Friendship
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*

object FriendsTable : Table() {
    val initiatorUserId = reference("initiator_user_id", UserTable).primaryKey()
    val receiverUserId = reference("receiver_user_id", UserTable).primaryKey()
    val confirmed = bool("confirmed").default(false)

    fun getFriendship(currentUserId: Int, targetUserId: Int): Friendship {
        val row = select { (FriendsTable.initiatorUserId.eq(currentUserId) and FriendsTable.receiverUserId.eq(targetUserId)) or (FriendsTable.initiatorUserId.eq(targetUserId) and FriendsTable.receiverUserId.eq(currentUserId)) }
            .singleOrNull()

        return when {
            row == null -> Friendship.NONE
            row[FriendsTable.confirmed] -> Friendship.CONFIRMED
            row[FriendsTable.initiatorUserId].value == currentUserId -> Friendship.REQUEST_SENT
            row[FriendsTable.receiverUserId].value == currentUserId -> Friendship.REQUEST_RECEIVED
            else -> throw IllegalStateException()
        }
    }

    fun getFriends(userId: Int): List<UserFriendship> = select { FriendsTable.initiatorUserId.eq(userId) or FriendsTable.receiverUserId.eq(userId) }
        .map { if (it[FriendsTable.initiatorUserId].value == userId)
            UserFriendship(
                userId = it[FriendsTable.receiverUserId].value,
                friendship = if (it[FriendsTable.confirmed]) Friendship.CONFIRMED else Friendship.REQUEST_SENT
            )
        else
            UserFriendship(
                userId = it[FriendsTable.initiatorUserId].value,
                friendship = if (it[FriendsTable.confirmed]) Friendship.CONFIRMED else Friendship.REQUEST_RECEIVED
            )
        }

    fun insertRequest(currentUserId: Int, targetUserId: Int) = insert {
        it[FriendsTable.initiatorUserId] = EntityID(currentUserId, UserTable)
        it[FriendsTable.receiverUserId] = EntityID(targetUserId, UserTable)
    }

    fun deleteFriendship(currentUserId: Int, targetUserId: Int) = deleteWhere {
        (FriendsTable.initiatorUserId.eq(currentUserId) and FriendsTable.receiverUserId.eq(targetUserId)) or (FriendsTable.initiatorUserId.eq(targetUserId) and FriendsTable.receiverUserId.eq(currentUserId))
    }

    fun confirmRequest(currentUserId: Int, targetUserId: Int) = update(
        where = { FriendsTable.initiatorUserId.eq(targetUserId) and FriendsTable.receiverUserId.eq(currentUserId) },
        body = { it[FriendsTable.confirmed] = true }
    )

    fun cancelRequest(currentUserId: Int, targetUserId: Int) = deleteWhere {
        FriendsTable.initiatorUserId.eq(currentUserId) and FriendsTable.receiverUserId.eq(targetUserId)
    }
}

data class UserFriendship(
    val userId: Int,
    val friendship: Friendship
)
