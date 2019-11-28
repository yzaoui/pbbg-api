package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.db.model.User
import com.bitwiserain.pbbg.domain.model.UserStats

interface UserUC {
    /**
     * Gets user by ID.
     */
    fun getUserById(userId: Int): User?

    /**
     * Registers a user.
     *
     * @throws UsernameNotAvailableException if [username] is not available.
     * @throws CredentialsFormatException if [username] or [password] don't match the required formats.
     */
    fun registerUser(username: String, password: String): Int

    /**
     * Gets user by credentials (username + password).
     */
    fun getUserIdByCredentials(username: String, password: String): Int?

    /**
     * Gets a user's stats by ID.
     */
    fun getUserStatsByUserId(userId: Int): UserStats

    /**
     * Change a user's password.
     *
     * @throws WrongCurrentPasswordException if [currentPassword] does not match the user's current password.
     * @throws UnconfirmedNewPasswordException if [confirmNewPassword] does not match [newPassword].
     * @throws NewPasswordNotNewException if [newPassword] is not different from [currentPassword].
     * @throws IllegalPasswordException if new password doesn't fulfill password format requirement.
     */
    fun changePassword(userId: Int, currentPassword: String, newPassword: String, confirmNewPassword: String)
}

class UsernameNotAvailableException(val username: String) : Exception()
class CredentialsFormatException(val usernameError: String?, val passwordError: String?) : Exception()
class WrongCurrentPasswordException : Exception()
class UnconfirmedNewPasswordException : Exception()
class NewPasswordNotNewException : Exception()
class IllegalPasswordException : Exception()
