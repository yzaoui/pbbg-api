package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.*
import com.bitwiserain.pbbg.db.repository.*
import com.bitwiserain.pbbg.db.repository.farm.PlotTable
import com.bitwiserain.pbbg.db.repository.market.MarketInventoryTable
import com.bitwiserain.pbbg.db.repository.market.MarketTable
import com.bitwiserain.pbbg.domain.model.MaterializedItem
import com.bitwiserain.pbbg.domain.model.MyUnitEnum.*
import com.bitwiserain.pbbg.domain.model.UserStats
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistory
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistoryInfo
import com.bitwiserain.pbbg.domain.usecase.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Clock
import java.time.temporal.ChronoUnit

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

    override fun changePassword(userId: Int, currentPassword: String, newPassword: String, confirmNewPassword: String): Unit = transaction(db) {
        // TODO: Consider checking if user exists
        val expectedPasswordHash = UserTable.select { UserTable.id.eq(userId) }
            .map { it[UserTable.passwordHash] }
            .single()

        // Make sure current password matches
        if (!BCryptHelper.verifyPassword(currentPassword, expectedPasswordHash)) throw WrongCurrentPasswordException()

        // Make sure new password is actually new
        if (currentPassword == newPassword) throw NewPasswordNotNewException()

        // Make sure new password was typed twice correctly
        if (newPassword != confirmNewPassword) throw UnconfirmedNewPasswordException()

        // Make sure new password fits format requirement
        if (!newPassword.matches(PASSWORD_REGEX.toRegex())) throw IllegalPasswordException()

        // At this point, new password is legal, so update
        UserTable.update({ UserTable.id.eq(userId) }) {
            it[UserTable.passwordHash] = BCryptHelper.hashPassword(newPassword)
        }
    }
}
