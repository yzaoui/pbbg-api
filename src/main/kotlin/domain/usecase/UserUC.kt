package pbbg.domain.usecase

import at.favre.lib.crypto.bcrypt.BCrypt
import pbbg.data.UserTable
import pbbg.data.model.User
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

interface UserUC {
    fun getUserById(userId: Int): User?
    fun getUserByUsername(username: String): User?
    fun usernameAvailable(username: String): Boolean
    fun registerUser(username: String, password: String): Int
    fun getUserIdByCredentials(username: String, password: String): Int?
}

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
