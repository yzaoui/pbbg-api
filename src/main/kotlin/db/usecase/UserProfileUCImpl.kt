package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.FriendsTable
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.model.UserProfile
import com.bitwiserain.pbbg.domain.model.friends.FriendInfo
import com.bitwiserain.pbbg.domain.usecase.TargetUserDoesNotExistException
import com.bitwiserain.pbbg.domain.usecase.UserProfileUC
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class UserProfileUCImpl(private val db: Database) : UserProfileUC {
    override fun getUserProfile(targetUserId: Int, currentUserId: Int?): UserProfile = transaction(db) {
        val targetUser = UserTable.getUserById(targetUserId) ?: throw TargetUserDoesNotExistException

        return@transaction UserProfile(
            id = targetUser.id,
            username = targetUser.username,
            friendship = currentUserId?.let { FriendsTable.getFriendship(it, targetUser.id) }
        )
    }

    override fun searchUsers(userId: Int, text: String): List<FriendInfo> = transaction(db) {
        // TODO: EXTREMELY inefficient
        return@transaction UserTable.searchUsers(text)
            .filterNot { it.id == userId }
            .map { FriendInfo(it.id, it.username, FriendsTable.getFriendship(userId, it.id)) }
    }
}
