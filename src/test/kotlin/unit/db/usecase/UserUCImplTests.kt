package com.bitwiserain.pbbg.test.unit.db.usecase

import com.bitwiserain.pbbg.SchemaHelper
import com.bitwiserain.pbbg.db.repository.UserStatsTable
import com.bitwiserain.pbbg.db.usecase.UserUCImpl
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.test.createTestUserAndGetId
import com.bitwiserain.pbbg.test.initDatabase
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class UserUCImplTests {
    private val db = initDatabase()
    private val userUC: UserUC = UserUCImpl(db)

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(db)
    }

    @Nested
    inner class ByCredentials {
        @Test
        fun `Given an existing user, when getting the user's ID by correct credentials, the ID should return`() {
            val expectedUserId = createTestUserAndGetId(db, username = "username24", password = "pass123")

            val actualUserId = userUC.getUserIdByCredentials("username24", "pass123")

            assertEquals(expectedUserId, actualUserId)
        }

        @Test
        fun `Given an existing user, when getting the user's ID by incorrect credentials, no ID should be returned`() {
            val expectedUserId = createTestUserAndGetId(db, username = "username24", password = "pass123")

            /* Test incorrect username */
            assertNull(userUC.getUserIdByCredentials("incorrecto17", "pass123"))

            /* Test incorrect password */
            assertNull(userUC.getUserIdByCredentials("username24", "pass12345"))

            /* Test incorrect username & password */
            assertNull(userUC.getUserIdByCredentials("incorrecto17", "pass12345"))
        }
    }

    @Nested
    inner class UserStats {
        @Test
        fun `When getting user stats by ID, the user stats should return`() {
            val userId = createTestUserAndGetId(db)

            transaction(db) {
                UserStatsTable.createUserStats(userId)
                UserStatsTable.updateGold(userId, 20)
                UserStatsTable.updateMiningExp(userId, 500)
            }

            val stats = userUC.getUserStatsByUserId(userId)

            assertEquals(20, stats.gold)
            assertEquals(500, stats.miningExp)
        }
    }
}
