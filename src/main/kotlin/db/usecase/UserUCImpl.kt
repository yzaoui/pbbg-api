package com.bitwiserain.pbbg.db.usecase

import at.favre.lib.crypto.bcrypt.BCrypt
import com.bitwiserain.pbbg.db.model.User
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.usecase.UserUC
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class UserUCImpl(private val db: Database) : UserUC {
    override fun getUserById(userId: Int): User? = transaction(db) {
        UserTable.select { UserTable.id.eq(userId) }
            .mapNotNull {
                User(it[UserTable.id].value, it[UserTable.username], it[UserTable.passwordHash])
            }
            .singleOrNull()
    }

    override fun getUserByUsername(username: String): User? = transaction(db) {
        UserTable.select { UserTable.username.eq(username) }
            .mapNotNull { User(it[UserTable.id].value, it[UserTable.username], it[UserTable.passwordHash]) }
            .singleOrNull()
    }

    override fun usernameAvailable(username: String): Boolean {
        return getUserByUsername(username) == null
    }

    override fun registerUser(username: String, password: String): Int = transaction(db) {
        UserTable.insertAndGetId {
            it[UserTable.username] = username
            it[UserTable.passwordHash] = BCrypt.withDefaults().hash(12, password.toByteArray())
        }.value
    }

    override fun getUserIdByCredentials(username: String, password: String): Int? {
        val user = getUserByUsername(username)
        return if (user != null && BCrypt.verifyer().verify(password.toByteArray(), user.passwordHash).verified) {
            user.id
        } else {
            null
        }
    }
}
