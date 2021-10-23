package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.db.repository.UserStatsTable
import com.bitwiserain.pbbg.domain.model.UserStats
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Gets a user's stats by ID.
 */
interface GetUserStatsUC {
    operator fun invoke(userId: Int): UserStats
}

class GetUserStatsUCImpl(private val db: Database, private val userStatsTable: UserStatsTable) : GetUserStatsUC {

    override fun invoke(userId: Int): UserStats = transaction(db) {
        // TODO: Consider checking if user exists
        userStatsTable.getUserStats(userId)
    }
}
