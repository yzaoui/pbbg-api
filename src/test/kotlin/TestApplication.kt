package com.bitwiserain.pbbg.test

import com.bitwiserain.pbbg.BCryptHelper
import com.bitwiserain.pbbg.SchemaHelper
import com.bitwiserain.pbbg.db.repository.UserTable
import org.h2.Driver
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

fun initDatabase(): Database = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", Driver::class.qualifiedName!!)
    .also { SchemaHelper.createTables(it) }

fun createTestUserAndGetId(db: Database, username: String = "username", password: String = "password"): EntityID<Int> = transaction(db) {
    UserTable.createUserAndGetId(username, BCryptHelper.hashPassword(password))
}
