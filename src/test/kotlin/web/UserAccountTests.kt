package com.bitwiserain.pbbg.test.web

import com.bitwiserain.pbbg.db.usecase.*
import com.bitwiserain.pbbg.mainWithDependencies
import com.bitwiserain.pbbg.test.initDatabase
import io.ktor.http.*
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class UserAccountTests {
    private val db = initDatabase()
    private val userUC = UserUCImpl(db)
    private val inventoryUC = InventoryUCImpl(db)
    private val miningUC = MiningUCImpl(db, inventoryUC)
    private val equipmentUC = EquipmentUCImpl(db)
    private val unitUC = UnitUCImpl(db)
    private val battleUC = BattleUCImpl(db)
    private val dexUC = DexUCImpl(db)

    @Test
    fun `Registering with legal credentials redirects to member index and sets session cookie`() = testApp {
        val registerRequest = handleRequest(HttpMethod.Post, "/register") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("username" to "testUsername1", "password" to "testPassword1").formUrlEncode())
        }.apply {
            assertEquals("/", response.headers[HttpHeaders.Location], "Register should redirect to index")
        }

        val sessionCookie = parseServerSetCookieHeader(registerRequest.response.headers[HttpHeaders.SetCookie]!!)
        assertEquals("pbbg_session", sessionCookie.name,"Register should return session cookie")

        handleRequest(HttpMethod.Get, "/") {
            sessionCookie.toString()
            addHeader(HttpHeaders.Cookie, renderCookieHeader(sessionCookie))
        }.apply {
            assertTrue(response.content?.contains("testUsername1") ?: false, "Index after registration should be member index page")
        }
    }

    @Test
    fun `Logging in is possible using session returned from registration`() = testApp {
        // Register and retrieve session cookie
        handleRequest(HttpMethod.Post, "/register") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("username" to "testUsername2", "password" to "testPassword2").formUrlEncode())
        }

        // Log out
        handleRequest(HttpMethod.Post, "/logout")

        handleRequest(HttpMethod.Post, "/login") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("username" to "testUsername2", "password" to "testPassword2").formUrlEncode())
        }.apply {
            assertEquals(
                "/",
                response.headers[HttpHeaders.Location],
                "Registering, logging out, then logging back in with the same credentials should return the user to the member index"
            )
        }
    }

    private fun testApp(block: TestApplicationEngine.() -> Unit) {
        withTestApplication({
            mainWithDependencies(userUC, inventoryUC, miningUC, equipmentUC, unitUC, battleUC, dexUC)
        }) { block() }
    }
}
