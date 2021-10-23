package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.BCryptHelper
import com.bitwiserain.pbbg.PASSWORD_REGEX
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.usecase.ChangePasswordUC.Result
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

/**
 * Changes a user's password.
 */
interface ChangePasswordUC {
    operator fun invoke(userId: Int, currentPassword: String, newPassword: String, confirmNewPassword: String): Result

    sealed interface Result {
        /**
         * Password changed successfully.
         */
        object Success : Result

        /**
         * Current password does not match the user's current password.
         */
        object WrongCurrentPasswordError : Result

        /**
         * New password confirmation does not match new password.
         */
        object UnconfirmedNewPasswordError : Result

        /**
         * New password is the same as current password.
         */
        object NewPasswordNotNewError : Result

        /**
         * New password doesn't fulfill password format requirement.
         */
        object IllegalPasswordError : Result
    }
}

class ChangePasswordUCImpl(private val db: Database) : ChangePasswordUC {

    override fun invoke(userId: Int, currentPassword: String, newPassword: String, confirmNewPassword: String): Result = transaction(db) {
        // TODO: Consider checking if user exists
        val expectedPasswordHash = UserTable.select { UserTable.id.eq(userId) }
            .map { it[UserTable.passwordHash] }
            .single()

        // Make sure current password matches
        if (!BCryptHelper.verifyPassword(currentPassword, expectedPasswordHash)) return@transaction Result.WrongCurrentPasswordError

        // Make sure new password is actually new
        if (currentPassword == newPassword) return@transaction Result.NewPasswordNotNewError

        // Make sure new password was typed twice correctly
        if (newPassword != confirmNewPassword) return@transaction Result.UnconfirmedNewPasswordError

        // Make sure new password fits format requirement
        if (!newPassword.matches(PASSWORD_REGEX.toRegex())) return@transaction Result.IllegalPasswordError

        // At this point, new password is legal, so update
        UserTable.update({ UserTable.id.eq(userId) }) {
            it[passwordHash] = BCryptHelper.hashPassword(newPassword)
        }

        return@transaction Result.Success
    }
}
