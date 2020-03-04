package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.model.User
import com.bitwiserain.pbbg.db.repository.FriendsTable
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.model.UserProfile
import com.bitwiserain.pbbg.domain.usecase.TargetUserDoesNotExistException
import com.bitwiserain.pbbg.domain.usecase.UserProfileUC
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class UserProfileUCImpl(private val db: Database) : UserProfileUC {
    override fun getUserProfile(targetUserId: Int, currentUser: User?): UserProfile = transaction(db) {
        val targetUser = UserTable.getUserById(targetUserId) ?: throw TargetUserDoesNotExistException

        return@transaction UserProfile(
            username = targetUser.username,
            friendship = currentUser?.let { currentUser -> FriendsTable.getFriendship(currentUser.id, targetUser.id) }
        )
    }
}
