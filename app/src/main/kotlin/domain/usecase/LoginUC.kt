package com.bitwiserain.pbbg.app.domain.usecase

import com.bitwiserain.pbbg.app.BCryptHelper
import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.UserTable
import com.bitwiserain.pbbg.app.domain.usecase.LoginUC.Result

/**
 * Login user by credentials (username + password).
 */
interface LoginUC {
    operator fun invoke(username: String, password: String): Result

    sealed interface Result {
        data class Success(val userId: Int) : Result
        data object CredentialsDontMatchError : Result
    }
}

class LoginUCImpl(private val transaction: Transaction, private val userTable: UserTable) : LoginUC {

    override fun invoke(username: String, password: String): Result {
        val user = transaction { userTable.getUserByUsername(username) }

        return if (user != null && BCryptHelper.verifyPassword(password, user.passwordHash)) {
            Result.Success(user.id)
        } else {
            Result.CredentialsDontMatchError
        }
    }
}
