package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.USERNAME_MAX_LENGTH
import com.bitwiserain.pbbg.db.model.User
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Instant

interface UserTable {

    fun createUserAndGetId(username: String, passwordHash: ByteArray, joinedInstant: Instant): Int

    fun getUserByUsername(username: String): User?

    fun getUserById(userId: Int): User?

    fun getUsersById(userIds: Iterable<Int>): Map<Int, User>

    fun userExists(userId: Int): Boolean

    fun searchUsers(text: String): List<User>

    fun updatePassword(userId: Int, newPassword: ByteArray)
}

class UserTableImpl : UserTable {

    object Exposed : IntIdTable(name = "User") {

        val username = varchar("username", USERNAME_MAX_LENGTH).uniqueIndex()
        val passwordHash = binary("password_hash", 60)
        val joinedEpochSeconds = long("joined_epoch_seconds")
    }

    override fun createUserAndGetId(username: String, passwordHash: ByteArray, joinedInstant: Instant): Int = Exposed.insertAndGetId {
        it[Exposed.username] = username
        it[Exposed.passwordHash] = passwordHash
        it[Exposed.joinedEpochSeconds] = joinedInstant.epochSecond
    }.value

    override fun getUserByUsername(username: String): User? = Exposed
        .select { Exposed.username.eq(username) }
        .singleOrNull()
        ?.toUser()

    override fun getUserById(userId: Int): User? = Exposed
        .select { Exposed.id.eq(userId) }
        .singleOrNull()
        ?.toUser()

    override fun getUsersById(userIds: Iterable<Int>): Map<Int, User> = Exposed
        .select { Exposed.id.inList(userIds) }
        .associate { it[Exposed.id].value to it.toUser() }

    override fun userExists(userId: Int): Boolean = Exposed.select(
        exists(
            Exposed.select { Exposed.id.eq(userId) }
        )
    ).any()

    override fun searchUsers(text: String): List<User> = Exposed.selectAll()
        .map { it.toUser() }
        .filter { it.username.contains(text, ignoreCase = true) }

    override fun updatePassword(userId: Int, newPassword: ByteArray) {
        Exposed.update({ Exposed.id.eq(userId) }) {
            it[passwordHash] = newPassword
        }
    }

    private fun ResultRow.toUser() = User(
        id = this[Exposed.id].value,
        username = this[Exposed.username],
        passwordHash = this[Exposed.passwordHash],
        joinedInstant = this[Exposed.joinedEpochSeconds].let(Instant::ofEpochSecond)
    )
}
