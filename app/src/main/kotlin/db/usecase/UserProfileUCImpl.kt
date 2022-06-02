package com.bitwiserain.pbbg.app.db.usecase

import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.FriendsTable
import com.bitwiserain.pbbg.app.db.repository.UserTable
import com.bitwiserain.pbbg.app.domain.model.UserProfile
import com.bitwiserain.pbbg.app.domain.model.friends.FriendInfo
import com.bitwiserain.pbbg.app.domain.usecase.TargetUserDoesNotExistException
import com.bitwiserain.pbbg.app.domain.usecase.UserProfileUC

class UserProfileUCImpl(private val transaction: Transaction, private val friendsTable: FriendsTable, private val userTable: UserTable) : UserProfileUC {

    override fun getUserProfile(targetUserId: Int, currentUserId: Int?): UserProfile = transaction {
        val targetUser = userTable.getUserById(targetUserId) ?: throw TargetUserDoesNotExistException

        return@transaction UserProfile(
            id = targetUser.id,
            username = targetUser.username,
            joinedInstant = targetUser.joinedInstant,
            friendship = currentUserId?.let { friendsTable.getFriendship(it, targetUser.id) }
        )
    }

    override fun searchUsers(userId: Int, text: String): List<FriendInfo> = transaction {
        // TODO: EXTREMELY inefficient
        return@transaction userTable.searchUsers(text)
            .filterNot { it.id == userId }
            .map { FriendInfo(it.id, it.username, friendsTable.getFriendship(userId, it.id)) }
    }
}
