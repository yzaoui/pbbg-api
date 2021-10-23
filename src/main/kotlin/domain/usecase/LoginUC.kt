package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.BCryptHelper
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.usecase.LoginUC.Result
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Login user by credentials (username + password).
 */
interface LoginUC {
    operator fun invoke(username: String, password: String): Result

    sealed interface Result {
        data class Success(val userId: Int) : Result
        object CredentialsDontMatchError : Result
    }
}

class LoginUCImpl(private val db: Database) : LoginUC {

    override fun invoke(username: String, password: String): Result {
        val user = transaction(db) { UserTable.getUserByUsername(username) }

        return if (user != null && BCryptHelper.verifyPassword(password, user.passwordHash)) {
            Result.Success(user.id)
        } else {
            Result.CredentialsDontMatchError
        }
    }
}
