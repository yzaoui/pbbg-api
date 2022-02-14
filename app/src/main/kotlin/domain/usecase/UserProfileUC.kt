package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.UserProfile
import com.bitwiserain.pbbg.domain.model.friends.FriendInfo

interface UserProfileUC {
    /**
     * Gets a given user's profile, optionally using the currently requesting user for additional details.
     *
     * @throws TargetUserDoesNotExistException when the target user does not exist.
     */
    fun getUserProfile(targetUserId: Int, currentUserId: Int?): UserProfile

    fun searchUsers(userId: Int, text: String): List<FriendInfo>
}

object TargetUserDoesNotExistException : Exception()
