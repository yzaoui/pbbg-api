package com.bitwiserain.pbbg.app.testintegration

import com.bitwiserain.pbbg.app.BCryptHelper
import com.bitwiserain.pbbg.app.SchemaHelper
import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.UserTable
import com.bitwiserain.pbbg.app.mainWithDependencies
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
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

fun TestApplicationEngine.registerUserAndGetToken(username: String = "username", password: String = "password") = handleRequest(HttpMethod.Post, "/api/register") {
    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    setBody(
        buildJsonObject {
            put("username", username)
            put("password", password)
        }.toString()
    )
}.run {
    Json.parseToJsonElement(response.content.orEmpty()).jsonObject.getValue("data").jsonObject.getValue("token").jsonPrimitive.content
}

fun testApp(clock: Clock, block: TestApplicationEngine.() -> Unit) {
    withTestApplication({
        (environment.config as MapApplicationConfig).apply {
            put("ktor.environment", "prod")
            put("jdbc.address", "h2:mem:test;DB_CLOSE_DELAY=-1")
            put("jwt.issuer", "https://pbbg-api.bitwiserain.com")
            put("jwt.realm", "PBBG API Server")
            put("jwt.secret", "eShVmYp3s6v9y\$B&E)H@McQfTjWnZr4t")
        }
        mainWithDependencies(clock)
    }, block)
}
