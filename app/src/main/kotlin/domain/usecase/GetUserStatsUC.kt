package com.bitwiserain.pbbg.app.domain.usecase

import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.UserStatsTable
import com.bitwiserain.pbbg.app.domain.model.UserStats

/**
 * Gets a user's stats by ID.
 */
interface GetUserStatsUC {
    operator fun invoke(userId: Int): UserStats
}

class GetUserStatsUCImpl(private val transaction: Transaction, private val userStatsTable: UserStatsTable) : GetUserStatsUC {

    override fun invoke(userId: Int): UserStats = transaction {
        // TODO: Consider checking if user exists
        userStatsTable.getUserStats(userId)
    }
}
