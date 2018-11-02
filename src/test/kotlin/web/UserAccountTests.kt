package com.bitwiserain.pbbg.test.web

import com.bitwiserain.pbbg.db.repository.*
import com.bitwiserain.pbbg.db.repository.battle.BattleEnemyTable
import com.bitwiserain.pbbg.db.repository.battle.BattleSessionTable
import com.bitwiserain.pbbg.db.usecase.*
import com.bitwiserain.pbbg.mainWithDependencies
import io.ktor.http.*
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.h2.Driver
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserAccountTests {
    private val db = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", Driver::class.qualifiedName!!)
    private val userUC = UserUCImpl(db)
    private val inventoryUC = InventoryUCImpl(db)
    private val miningUC = MiningUCImpl(db, inventoryUC)
    private val equipmentUC = EquipmentUCImpl(db)
    private val unitUC = UnitUCImpl(db)
    private val battleUC = BattleUCImpl(db)

    init {
        transaction(db) {
            SchemaUtils.create(
                UserTable, MineSessionTable, MineCellTable, EquipmentTable, InventoryTable, UserStatsTable,
                UnitTable, SquadTable, BattleSessionTable, BattleEnemyTable
            )
        }
    }

    @Test
    fun `Registering with legal credentials redirects to member index and sets session cookie`() = testApp {
        val username = "testUsername1"
        val password = "testPassword1"

        val registerRequest = handleRequest(HttpMethod.Post, "/register") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("username" to username, "password" to password).formUrlEncode())
        }.apply {
            assertEquals("/", response.headers[HttpHeaders.Location], "Register should redirect to index")
        }

        val sessionCookie = parseServerSetCookieHeader(registerRequest.response.headers[HttpHeaders.SetCookie]!!)
        assertEquals("pbbg_session", sessionCookie.name,"Register should return session cookie")

        handleRequest(HttpMethod.Get, "/") {
            sessionCookie.toString()
            addHeader(HttpHeaders.Cookie, renderCookieHeader(sessionCookie))
        }.apply {
            assertTrue(response.content?.contains(username) ?: false, "Index after registration should be member index page")
        }
    }

    @Test
    fun `Logging in is possible using session returned from registration`() = testApp {
        val username = "testUsername2"
        val password = "testPassword2"
        // Register and retrieve session cookie
        handleRequest(HttpMethod.Post, "/register") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("username" to username, "password" to password).formUrlEncode())
        }

        // Log out
        handleRequest(HttpMethod.Post, "/logout")

        handleRequest(HttpMethod.Post, "/login") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(listOf("username" to username, "password" to password).formUrlEncode())
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
            mainWithDependencies(userUC, inventoryUC, miningUC, equipmentUC, unitUC, battleUC)
        }) { block() }
    }
}
