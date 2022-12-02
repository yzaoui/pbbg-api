package com.bitwiserain.pbbg.app.db.repository

import com.bitwiserain.pbbg.app.domain.model.friends.Friendship
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

interface FriendsTable {

    fun getFriendship(currentUserId: Int, targetUserId: Int): Friendship

    fun getFriends(userId: Int): List<UserFriendship>

    fun insertRequest(currentUserId: Int, targetUserId: Int)

    fun deleteFriendship(currentUserId: Int, targetUserId: Int)

    fun confirmRequest(currentUserId: Int, targetUserId: Int)

    fun cancelRequest(currentUserId: Int, targetUserId: Int)
}

class FriendsTableImpl : FriendsTable {

    object Exposed : Table(name = "Friends") {

        val initiatorUserId = reference("initiator_user_id", UserTableImpl.Exposed)
        val receiverUserId = reference("receiver_user_id", UserTableImpl.Exposed)
        override val primaryKey = PrimaryKey(initiatorUserId, receiverUserId)
        val confirmed = bool("confirmed").default(false)
    }

    override fun getFriendship(currentUserId: Int, targetUserId: Int): Friendship {
        val row = Exposed.select {
            (Exposed.initiatorUserId.eq(currentUserId) and Exposed.receiverUserId.eq(targetUserId)) or (Exposed.initiatorUserId.eq(targetUserId) and Exposed.receiverUserId.eq(currentUserId))
        }.singleOrNull()

        return when {
            row == null -> Friendship.NONE
            row[Exposed.confirmed] -> Friendship.CONFIRMED
            row[Exposed.initiatorUserId].value == currentUserId -> Friendship.REQUEST_SENT
            row[Exposed.receiverUserId].value == currentUserId -> Friendship.REQUEST_RECEIVED
            else -> throw IllegalStateException()
        }
    }

    override fun getFriends(userId: Int): List<UserFriendship> = Exposed.select { Exposed.initiatorUserId.eq(userId) or Exposed.receiverUserId.eq(userId) }
        .map { if (it[Exposed.initiatorUserId].value == userId)
            UserFriendship(
                userId = it[Exposed.receiverUserId].value,
                friendship = if (it[Exposed.confirmed]) Friendship.CONFIRMED else Friendship.REQUEST_SENT
            )
        else
            UserFriendship(
                userId = it[Exposed.initiatorUserId].value,
                friendship = if (it[Exposed.confirmed]) Friendship.CONFIRMED else Friendship.REQUEST_RECEIVED
            )
        }

    override fun insertRequest(currentUserId: Int, targetUserId: Int) {
        Exposed.insert {
            it[Exposed.initiatorUserId] = EntityID(currentUserId, UserTableImpl.Exposed)
            it[Exposed.receiverUserId] = EntityID(targetUserId, UserTableImpl.Exposed)
        }
    }

    override fun deleteFriendship(currentUserId: Int, targetUserId: Int) {
        Exposed.deleteWhere {
            (Exposed.initiatorUserId.eq(currentUserId) and Exposed.receiverUserId.eq(targetUserId)) or (Exposed.initiatorUserId.eq(targetUserId) and Exposed.receiverUserId.eq(currentUserId))
        }
    }

    override fun confirmRequest(currentUserId: Int, targetUserId: Int) {
        Exposed.update(
            where = { Exposed.initiatorUserId.eq(targetUserId) and Exposed.receiverUserId.eq(currentUserId) },
            body = { it[Exposed.confirmed] = true }
        )
    }

    override fun cancelRequest(currentUserId: Int, targetUserId: Int) {
        Exposed.deleteWhere {
            Exposed.initiatorUserId.eq(currentUserId) and Exposed.receiverUserId.eq(targetUserId)
        }
    }
}

data class UserFriendship(
    val userId: Int,
    val friendship: Friendship
)
