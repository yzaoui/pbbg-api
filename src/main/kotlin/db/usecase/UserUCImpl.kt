package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.BCryptHelper
import com.bitwiserain.pbbg.db.repository.UserStatsTable
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.model.UserStats
import com.bitwiserain.pbbg.domain.usecase.UserUC
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class UserUCImpl(private val db: Database) : UserUC {

    override fun getUserIdByCredentials(username: String, password: String): Int? {
        val user = transaction(db) {
            UserTable.getUserByUsername(username)
        }

        return if (user != null && BCryptHelper.verifyPassword(password, user.passwordHash)) {
            user.id
        } else {
            null
        }
    }

    override fun getUserStatsByUserId(userId: Int): UserStats = transaction(db) {
        // TODO: Consider checking if user exists
        UserStatsTable.getUserStats(userId)
    }
}
