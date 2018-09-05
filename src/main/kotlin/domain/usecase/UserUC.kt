package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.db.model.User

interface UserUC {
    fun getUserById(userId: Int): User?
    fun getUserByUsername(username: String): User?
    fun usernameAvailable(username: String): Boolean
    fun registerUser(username: String, password: String): Int
    fun getUserIdByCredentials(username: String, password: String): Int?
}
