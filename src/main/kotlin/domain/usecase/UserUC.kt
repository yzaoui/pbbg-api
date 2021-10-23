package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.UserStats

interface UserUC {
    /**
     * Gets a user's stats by ID.
     */
    fun getUserStatsByUserId(userId: Int): UserStats
}
