package com.bitwiserain.pbbg.test.integration

import com.bitwiserain.pbbg.SchemaHelper
import com.bitwiserain.pbbg.test.MutableClock
import com.bitwiserain.pbbg.test.initDatabase
import com.bitwiserain.pbbg.test.integration.api.GETInventory
import com.bitwiserain.pbbg.test.integration.api.GETPlots
import com.bitwiserain.pbbg.test.integration.api.POSTPlant
import com.bitwiserain.pbbg.test.integration.model.Inventory
import com.bitwiserain.pbbg.test.integration.model.farm.Plot
import com.bitwiserain.pbbg.test.integration.requestbody.PlantRequest
import com.bitwiserain.pbbg.test.registerUserAndGetToken
import com.bitwiserain.pbbg.test.testApp
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class FarmTests {
    private val clock = MutableClock()
    private val db = initDatabase()

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(db)
    }

    @Test
    fun `Given a new user, farm should have one plot`() = testApp(clock) {
        val response = GETPlots(registerUserAndGetToken())
        assertEquals(HttpStatusCode.OK, response.status())

        val body = Json.parse(JsonObject.serializer(), response.content.orEmpty())
        assertEquals("success", body["status"]?.content)

        val plots: List<Plot> = assertDoesNotThrow {
            Json.parse(Plot.serializer().list, Json.stringify(JsonElement.serializer(), body.getValue("data")))
        }

        assertEquals(1, plots.size, "There should initially be 1 plot")
        assertNull(plots.single().plant, "Plot should initially be empty")
    }

    @Test
    fun `When a user plants a plant, the plot in the response should have the plant in it, and the item should be removed`() = testApp(clock) {
        val token = registerUserAndGetToken()

        val plot = GETPlots(token).run {
            Json.parse(Plot.serializer().list, Json.stringify(JsonElement.serializer(), Json.parse(JsonObject.serializer(), content.orEmpty()).getValue("data"))).single()
        }

        /* Retrieve apple sapling from initial inventory */
        val appleSaplingInInventory = GETInventory(token, filter = "plantable").run {
            Json.parse(Inventory.serializer(), Json.stringify(JsonElement.serializer(), Json.parse(JsonObject.serializer(), content.orEmpty()).getValue("data")))
                .items.single { it.item.baseItem.friendlyName == "Apple Sapling" }.item
        }
        val initialAppleSaplingQuantity = appleSaplingInInventory.quantity!!

        /* Plant apple sapling */
        val plantResponse = POSTPlant(token, PlantRequest(
            plotId = plot.id,
            itemId = appleSaplingInInventory.id
        ))

        assertEquals(HttpStatusCode.OK, plantResponse.status())
        val plantResponseBody = Json.parse(JsonObject.serializer(), plantResponse.content.orEmpty())
        assertEquals("success", plantResponseBody["status"]?.content)

        val occupiedPlot = assertDoesNotThrow {
            Json.parse(Plot.serializer(), Json.stringify(JsonElement.serializer(), plantResponseBody.getValue("data")))
        }

        assertNotNull(occupiedPlot.plant, "Plot should be occupied after planting.")

        /* Check apple sapling was depleted */
        val newAppleSaplingQuantity = GETInventory(token).run {
            Json.parse(Inventory.serializer(), Json.stringify(JsonElement.serializer(), Json.parse(JsonObject.serializer(), content.orEmpty()).getValue("data")))
                .items.single { it.item.baseItem.friendlyName == "Apple Sapling" }.item.quantity!!
        }

        assertEquals(initialAppleSaplingQuantity - 1, newAppleSaplingQuantity, "Apple sapling quantity should decrease by 1 after planting 1.")
    }
}
