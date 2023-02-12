package com.bitwiserain.pbbg.app.testintegration

import com.bitwiserain.pbbg.app.SchemaHelper
import com.bitwiserain.pbbg.app.testintegration.api.GETInventory
import com.bitwiserain.pbbg.app.testintegration.model.Inventory
import com.bitwiserain.pbbg.app.testintegration.response.RegisterResponse
import com.bitwiserain.pbbg.app.testintegration.response.UserResponse
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
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
    private val transaction = initDatabase()
    private val clock = MutableClock()

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(transaction)
    }

    @Test
    fun `Given valid credentials, when registering, a successful response should return with an auth token`() = testApp(clock) {
        val response = client.post("/api/register") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(
                buildJsonObject {
                    put("username", "username")
                    put("password", "password")
                }.toString()
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("success", body["status"]?.let { it.jsonPrimitive.content })

        assertDoesNotThrow {
            Json.decodeFromJsonElement<RegisterResponse>(body.getValue("data"))
        }
    }

    @Test
    fun `When registering successfully, user should have 0 gold, 0 mining exp, and 0 farming exp`() = testApp(clock) {
        val response = client.get("/api/user-stats") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(HttpHeaders.Authorization, "Bearer ${registerUserAndGetToken()}")
        }
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
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

    @Test
    fun `When registering successfully, user should have 1 ice pick, 2 apple saplings, 5 tomato seeds, and nothing equipped`() = testApp(clock) {
        val inventoryResponse = GETInventory(registerUserAndGetToken())

        val body = Json.parseToJsonElement(inventoryResponse.bodyAsText()).jsonObject

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
