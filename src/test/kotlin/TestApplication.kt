package com.bitwiserain.pbbg.test

import com.bitwiserain.pbbg.BCryptHelper
import com.bitwiserain.pbbg.SchemaHelper
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.mainWithDependencies
import io.ktor.config.MapApplicationConfig
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonLiteral
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json
import org.h2.Driver
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Clock

fun initDatabase(): Database = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", Driver::class.qualifiedName!!)
    .also { SchemaHelper.createTables(it) }

fun createTestUserAndGetId(db: Database, username: String = "username", password: String = "password"): Int = transaction(db) {
    UserTable.createUserAndGetId(username, BCryptHelper.hashPassword(password))
}

fun TestApplicationEngine.registerUserAndGetToken(username: String = "username", password: String = "password") = handleRequest(HttpMethod.Post, "/api/register") {
    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    setBody(
        json {
            "username" to username
            "password" to password
        }.toString()
    )
}.run {
    (Json.parse(JsonObject.serializer(), response.content.orEmpty()).getValue("data").jsonObject.getAs<JsonLiteral>("token")).content
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
