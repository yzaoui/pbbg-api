package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.FriendsTable
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.model.friends.FriendInfo
import com.bitwiserain.pbbg.domain.model.friends.Friends
import com.bitwiserain.pbbg.domain.usecase.FriendsUC
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class FriendsUCImpl(private val db: Database) : FriendsUC {
    override fun getFriends(userId: Int): Friends = transaction(db) {
        // TODO: Could combine these into a single query
        val friendships = FriendsTable.getFriends(userId)
        val users = UserTable.getUsersById(friendships.map { it.userId })

        Friends(friendships.map { FriendInfo(it.userId, users.getValue(it.userId).username, it.friendship) })
    }
}
