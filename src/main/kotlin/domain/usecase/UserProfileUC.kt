package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.db.model.User
import com.bitwiserain.pbbg.domain.model.UserProfile

interface UserProfileUC {
    /**
     * Gets a given user's profile, optionally using the currently requesting user for additional details.
     *
     * @throws TargetUserDoesNotExistException when the target user does not exist.
     */
    fun getUserProfile(targetUserId: Int, currentUser: User?): UserProfile
}

object TargetUserDoesNotExistException : Exception()
