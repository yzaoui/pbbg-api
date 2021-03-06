package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.USERNAME_MAX_LENGTH
import com.bitwiserain.pbbg.db.model.User
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*

object UserTable : IntIdTable() {
    val username = varchar("username", USERNAME_MAX_LENGTH).uniqueIndex()
    val passwordHash = binary("password_hash", 60)

    fun createUserAndGetId(username: String, passwordHash: ByteArray): Int {
        return insertAndGetId {
            it[UserTable.username] = username
            it[UserTable.passwordHash] = passwordHash
        }.value
    }

    fun getUserByUsername(username: String): User? = select { UserTable.username.eq(username) }
        .singleOrNull()
        ?.toUser()

    fun getUserById(userId: Int): User? = select { UserTable.id.eq(userId) }
        .singleOrNull()
        ?.toUser()

    fun getUsersById(userIds: Iterable<Int>): Map<Int, User> = select { UserTable.id.inList(userIds) }
        .associate { it[UserTable.id].value to it.toUser() }

    fun userExists(userId: Int): Boolean = select(
        exists(
            select { UserTable.id.eq(userId) }
        )
    ).any()

    fun searchUsers(text: String): List<User> = selectAll()
        .map { it.toUser() }
        .filter { it.username.contains(text, ignoreCase = true) }

    private fun ResultRow.toUser() = User(this[id].value, this[username], this[passwordHash])
}
