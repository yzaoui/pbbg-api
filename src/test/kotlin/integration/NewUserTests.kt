package com.bitwiserain.pbbg.test.integration

import com.bitwiserain.pbbg.SchemaHelper
import com.bitwiserain.pbbg.test.MutableClock
import com.bitwiserain.pbbg.test.initDatabase
import com.bitwiserain.pbbg.test.integration.api.GETInventory
import com.bitwiserain.pbbg.test.integration.model.Inventory
import com.bitwiserain.pbbg.test.integration.response.RegisterResponse
import com.bitwiserain.pbbg.test.integration.response.UserResponse
import com.bitwiserain.pbbg.test.registerUserAndGetToken
import com.bitwiserain.pbbg.test.testApp
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import kotlinx.serialization.json.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class NewUserTests {
    private val db = initDatabase()
    private val clock = MutableClock()

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(db)
    }

    @Test
    fun `Given valid credentials, when registering, a successful response should return with an auth token`() = testApp(clock) {
        handleRequest(HttpMethod.Post, "/api/register") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(
                buildJsonObject {
                    put("username", "username")
                    put("password", "password")
                }.toString()
            )
        }.apply {
            assertEquals(HttpStatusCode.OK, response.status())

            val body = Json.parseToJsonElement(response.content.orEmpty()).jsonObject
            assertEquals("success", body["status"]?.let { it.jsonPrimitive.content })

            assertDoesNotThrow {
                Json.decodeFromJsonElement<RegisterResponse>(body.getValue("data"))
            }
        }
    }

    @Test
    fun `When registering successfully, user should have 0 gold, 0 mining exp, and 0 farming exp`() = testApp(clock) {
        handleRequest(HttpMethod.Get, "/api/user-stats") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            addHeader(HttpHeaders.Authorization, "Bearer ${registerUserAndGetToken()}")
        }.apply {
            val body = Json.parseToJsonElement(response.content.orEmpty()).jsonObject
            assertEquals("success", body["status"]?.let { it.jsonPrimitive.content })

            val userStats = assertDoesNotThrow {
                Json.decodeFromJsonElement<UserResponse>(body.getValue("data"))
            }

            assertEquals(0, userStats.gold, "Gold amount should be 0.")
            assertEquals(1, userStats.mining.level, "Mining level should be 1.")
            assertEquals(0, userStats.mining.relativeExp, "Mining experience should be 0.")
            assertEquals(1, userStats.farming.level, "Farming level should be 1.")
            assertEquals(0, userStats.farming.relativeExp, "Farming experience should be 0.")
        }
    }

    @Test
    fun `When registering successfully, user should have 1 ice pick, 2 apple saplings, 5 tomato seeds, and nothing equipped`() = testApp(clock) {
        val inventoryResponse = GETInventory(registerUserAndGetToken())

        val body = Json.parseToJsonElement(inventoryResponse.content.orEmpty()).jsonObject

        val inventory = assertDoesNotThrow {
            Json.decodeFromJsonElement<Inventory>(body.getValue("data"))
        }

        assertEquals(3, inventory.items.size, "Should have 3 items in inventory.")

        listOf("Apple Sapling" to 2, "Tomato Seed" to 5).forEach { (expectedItemName, expectedItemQuantity) ->
            inventory.items.map { it.item }.find { it.baseItem.friendlyName == expectedItemName }.let {
                assertNotNull(it, "$expectedItemName should be in inventory.")
                assertEquals(expectedItemQuantity, it.quantity, "There should be $expectedItemQuantity $expectedItemName in inventory.")
            }
        }

        val icePick = inventory.items.find { it.item.baseItem.friendlyName == "Ice Pick" }
        assertNotNull(icePick, "Ice Pick should be in inventory")
        assertFalse(icePick.equipped!!, "Ice Pick is initially unequipped.")

        assertNull(inventory.equipment.pickaxe, "No pickaxe should initially be equipped.")
    }
}
