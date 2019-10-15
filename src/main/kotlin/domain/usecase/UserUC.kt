package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.db.model.User
import com.bitwiserain.pbbg.domain.model.UserStats

interface UserUC {
    fun getUserById(userId: Int): User?
    fun usernameAvailable(username: String): Boolean
    fun registerUser(username: String, password: String): Int
    fun getUserIdByCredentials(username: String, password: String): Int?
    fun getUserStatsByUserId(userId: Int): UserStats

    /**
     * @throws WrongCurrentPasswordException if [currentPassword] does not match the user's current password.
     * @throws UnconfirmedNewPasswordException if [confirmNewPassword] does not match [newPassword].
     * @throws IllegalPasswordException if new password doesn't fulfill password format requirement.
     */
    fun changePassword(userId: Int, currentPassword: String, newPassword: String, confirmNewPassword: String)
}

class WrongCurrentPasswordException : Exception()
class UnconfirmedNewPasswordException : Exception()
class IllegalPasswordException : Exception()
