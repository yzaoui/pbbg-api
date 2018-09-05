package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.USERNAME_MAX_LENGTH
import org.jetbrains.exposed.dao.IntIdTable

object UserTable : IntIdTable() {
    val username = varchar("username", USERNAME_MAX_LENGTH).uniqueIndex()
    val passwordHash = binary("password_hash", 60)
}
