package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.friends.Friendship
import org.jetbrains.exposed.sql.*

object FriendsTable : Table() {
    val userId1 = reference("user_id_1", UserTable).primaryKey()
    val userId2 = reference("user_id_2", UserTable).primaryKey()
    val confirmed = bool("confirmed")

    fun getFriendship(currentUserId: Int, targetUserId: Int): Friendship {
        val row = select { (FriendsTable.userId1.eq(currentUserId) and FriendsTable.userId2.eq(targetUserId)) or (FriendsTable.userId1.eq(targetUserId) and FriendsTable.userId2.eq(currentUserId)) }
            .singleOrNull()

        return when {
            row == null -> Friendship.NONE
            row[FriendsTable.confirmed] -> Friendship.CONFIRMED
            row[FriendsTable.userId1].value == currentUserId -> Friendship.REQUEST_SENT
            row[FriendsTable.userId2].value == currentUserId -> Friendship.REQUEST_RECEIVED
            else -> throw IllegalStateException()
        }
    }

    fun getFriends(userId: Int): List<UserFriendship> = select { FriendsTable.userId1.eq(userId) or FriendsTable.userId2.eq(userId) }
        .map { if (it[FriendsTable.userId1].value == userId)
            UserFriendship(
                userId = it[FriendsTable.userId2].value,
                friendship = if (it[FriendsTable.confirmed]) Friendship.CONFIRMED else Friendship.REQUEST_SENT
            )
        else
            UserFriendship(
                userId = it[FriendsTable.userId1].value,
                friendship = if (it[FriendsTable.confirmed]) Friendship.CONFIRMED else Friendship.REQUEST_RECEIVED
            )
        }
}

data class UserFriendship(
    val userId: Int,
    val friendship: Friendship
)
