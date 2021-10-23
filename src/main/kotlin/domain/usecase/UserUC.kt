package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.UserStats

interface UserUC {
    /**
     * Gets user by credentials (username + password).
     */
    fun getUserIdByCredentials(username: String, password: String): Int?

    /**
     * Gets a user's stats by ID.
     */
    fun getUserStatsByUserId(userId: Int): UserStats
}
