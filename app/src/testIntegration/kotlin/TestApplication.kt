package com.bitwiserain.pbbg.app.testintegration

import com.bitwiserain.pbbg.app.BCryptHelper
import com.bitwiserain.pbbg.app.SchemaHelper
import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.UserTable
import com.bitwiserain.pbbg.app.mainWithDependencies
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.h2.Driver
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Clock

fun initDatabase(): Transaction {
    val db = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", Driver::class.qualifiedName!!)
    val transaction: Transaction = object : Transaction {
        override fun <T> invoke(block: () -> T): T = transaction(db) { block() }
    }
    SchemaHelper.createTables(transaction)
    return transaction
}

fun createTestUserAndGetId(transaction: Transaction, userTable: UserTable, username: String = "username", password: String = "password"): Int = transaction {
    userTable.createUserAndGetId(username, BCryptHelper.hashPassword(password), Clock.systemUTC().instant())
}

suspend fun ApplicationTestBuilder.registerUserAndGetToken(
    username: String = "username", password: String = "password"
) = client.post("/api/register") {
    header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    setBody(
        buildJsonObject {
            put("username", username)
            put("password", password)
        }.toString()
    )
}.run {
    Json.parseToJsonElement(bodyAsText()).jsonObject.getValue("data").jsonObject.getValue("token").jsonPrimitive.content
}

fun testApp(clock: Clock, block: suspend ApplicationTestBuilder.() -> Unit) = testApplication {
    environment {
        config = MapApplicationConfig().apply {
            put("ktor.environment", "prod")
            put("jdbc.address", "h2:mem:test;DB_CLOSE_DELAY=-1")
            put("jwt.issuer", "PBBG")
            put("jwt.realm", "PBBG API Server")
            put("jwt.secret", "eShVmYp3s6v9y\$B&E)H@McQfTjWnZr4t")
        }
    }
    application {
        mainWithDependencies(clock)
    }
    block()
}
