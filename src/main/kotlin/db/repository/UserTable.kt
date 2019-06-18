package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.USERNAME_MAX_LENGTH
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.insertAndGetId

object UserTable : IntIdTable() {
    val username = varchar("username", USERNAME_MAX_LENGTH).uniqueIndex()
    val passwordHash = binary("password_hash", 60)

    fun createUserAndGetId(username: String, passwordHash: ByteArray): EntityID<Int> {
        return insertAndGetId {
            it[UserTable.username] = username
            it[UserTable.passwordHash] = passwordHash
        }
    }
}
