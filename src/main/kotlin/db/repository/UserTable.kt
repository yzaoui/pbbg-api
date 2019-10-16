package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.USERNAME_MAX_LENGTH
import com.bitwiserain.pbbg.db.model.User
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select

object UserTable : IntIdTable() {
    val username = varchar("username", USERNAME_MAX_LENGTH).uniqueIndex()
    val passwordHash = binary("password_hash", 60)

    fun createUserAndGetId(username: String, passwordHash: ByteArray): EntityID<Int> {
        return insertAndGetId {
            it[UserTable.username] = username
            it[UserTable.passwordHash] = passwordHash
        }
    }

    fun getUserByUsername(username: String): User? = select { UserTable.username.eq(username) }
        .singleOrNull()
        ?.toUser()

    fun getUserById(userId: Int): User? = select { UserTable.id.eq(userId) }
        .singleOrNull()
        ?.toUser()

    private fun ResultRow.toUser() = User(this[id].value, this[username], this[passwordHash])
}
