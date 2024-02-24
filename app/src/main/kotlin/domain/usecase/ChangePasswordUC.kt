package com.bitwiserain.pbbg.app.domain.usecase

import com.bitwiserain.pbbg.app.BCryptHelper
import com.bitwiserain.pbbg.app.PASSWORD_REGEX
import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.UserTable
import com.bitwiserain.pbbg.app.domain.usecase.ChangePasswordUC.Result

/**
 * Changes a user's password.
 */
interface ChangePasswordUC {
    operator fun invoke(userId: Int, currentPassword: String, newPassword: String, confirmNewPassword: String): Result

    sealed interface Result {
        /**
         * Password changed successfully.
         */
        data object Success : Result

        /**
         * Current password does not match the user's current password.
         */
        data object WrongCurrentPasswordError : Result

        /**
         * New password confirmation does not match new password.
         */
        data object UnconfirmedNewPasswordError : Result

        /**
         * New password is the same as current password.
         */
        data object NewPasswordNotNewError : Result

        /**
         * New password doesn't fulfill password format requirement.
         */
        data object IllegalPasswordError : Result
    }
}

class ChangePasswordUCImpl(private val transaction: Transaction, private val userTable: UserTable) : ChangePasswordUC {

    override fun invoke(userId: Int, currentPassword: String, newPassword: String, confirmNewPassword: String): Result = transaction {
        // TODO: Consider checking if user exists
        val expectedPasswordHash = userTable.getUserById(userId)!!.passwordHash

        // Make sure current password matches
        if (!BCryptHelper.verifyPassword(currentPassword, expectedPasswordHash)) return@transaction Result.WrongCurrentPasswordError

        // Make sure new password is actually new
        if (currentPassword == newPassword) return@transaction Result.NewPasswordNotNewError

        // Make sure new password was typed twice correctly
        if (newPassword != confirmNewPassword) return@transaction Result.UnconfirmedNewPasswordError

        // Make sure new password fits format requirement
        if (!newPassword.matches(PASSWORD_REGEX.toRegex())) return@transaction Result.IllegalPasswordError

        // At this point, new password is legal, so update
        userTable.updatePassword(userId, BCryptHelper.hashPassword(newPassword))

        return@transaction Result.Success
    }
}
