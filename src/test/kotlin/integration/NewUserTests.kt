package com.bitwiserain.pbbg.test.integration

import com.bitwiserain.pbbg.SchemaHelper
import com.bitwiserain.pbbg.main
import com.bitwiserain.pbbg.test.initDatabase
import com.bitwiserain.pbbg.test.integration.response.InventoryResponse
import com.bitwiserain.pbbg.test.integration.response.RegisterResponse
import com.bitwiserain.pbbg.test.integration.response.UserResponse
import io.ktor.config.MapApplicationConfig
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonLiteral
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import kotlinx.serialization.parse
import kotlinx.serialization.stringify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ImplicitReflectionSerializer
@KtorExperimentalAPI
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class NewUserTests {
    private val db = initDatabase()

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(db)
    }

    @Test
    fun `Given valid credentials, when registering, a successful response should return with an auth token`() = testApp {
        registerUser().apply {
            assertEquals(HttpStatusCode.OK, response.status())

            val body = Json.parse<JsonObject>(response.content.orEmpty())
            assertEquals("success", body["status"]?.content)

            assertDoesNotThrow {
                Json.parse<RegisterResponse>(Json.stringify(body["data"]!!))
            }
        }
    }

    @Test
    fun `When registering successfully, user should have 0 gold and 0 mining exp`() = testApp {
        handleRequest(HttpMethod.Get, "/api/user") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            addHeader(HttpHeaders.Authorization, "Bearer ${registerUserAndGetToken()}")
        }.apply {
            val body = Json.parse<JsonObject>(response.content.orEmpty())
            assertEquals("success", body["status"]?.content)

            val userStats = assertDoesNotThrow {
                Json.parse<UserResponse>(Json.stringify(body["data"]!!))
            }

            assertEquals(0, userStats.gold, "Gold amount should be 0.")
            assertEquals(1, userStats.mining.level, "Mining level should be 1.")
            assertEquals(0, userStats.mining.relativeExp, "Mining experience should be 0.")
        }
    }

    @Test
    fun `When registering successfully, user should only have an ice pick in inventory`() = testApp {
        handleRequest(HttpMethod.Get, "/api/inventory") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            addHeader(HttpHeaders.Authorization, "Bearer ${registerUserAndGetToken()}")
        }.apply {
            val body = Json.parse<JsonObject>(response.content.orEmpty())

            val inventory = assertDoesNotThrow {
                Json.parse<InventoryResponse>(Json.stringify(body["data"]!!))
            }

            assertEquals(1, inventory.items.size, "Should only have 1 item in inventory.")

            val pick = inventory.items.single()
            assertEquals("Ice Pick", pick.item.baseItem.friendlyName, "The held item is an Ice Pick.")
            assertTrue(pick.equipped == false, "Ice Pick is initially unequipped.")

            assertNull(inventory.equipment.pickaxe, "No pickaxe should initially be equipped.")
        }
    }

    private fun testApp(block: TestApplicationEngine.() -> Unit) {
        withTestApplication({
            (environment.config as MapApplicationConfig).apply {
                put("ktor.environment", "prod")
                put("jdbc.address", "h2:mem:test;DB_CLOSE_DELAY=-1")
                put("jwt.issuer", "https://pbbg-api.bitwiserain.com")
                put("jwt.realm", "PBBG API Server")
                put("jwt.secret", "eShVmYp3s6v9y\$B&E)H@McQfTjWnZr4t")
            }
            main()
        }, block)
    }

    private fun TestApplicationEngine.registerUser() = handleRequest(HttpMethod.Post, "/api/register") {
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(Json.stringify(mapOf(
            "username" to "user27",
            "password" to "qwerty123"
        )))
    }

    private fun TestApplicationEngine.registerUserAndGetToken() = registerUser().run {
        (Json.parse<JsonObject>(response.content.orEmpty())["data"]!!.jsonObject.getAs<JsonLiteral>("token")).content
    }
}
