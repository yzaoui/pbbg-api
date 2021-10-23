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

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class UserUCImplTests {
    private val db = initDatabase()
    private val userUC: UserUC = UserUCImpl(db)

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(db)
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
