package com.bitwiserain.pbbg.test.domain.usecase

import com.bitwiserain.pbbg.SchemaHelper
import com.bitwiserain.pbbg.db.repository.UserStatsTable
import com.bitwiserain.pbbg.domain.usecase.GetUserStatsUCImpl
import com.bitwiserain.pbbg.test.createTestUserAndGetId
import com.bitwiserain.pbbg.test.initDatabase
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GetUserStatsUCImplTest {

    private val db = initDatabase()
    private val getUserStats = GetUserStatsUCImpl(db)

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(db)
    }

    @Test
    fun `When getting user stats by ID, the user stats should return`() {
        val userId = createTestUserAndGetId(db)

        transaction(db) {
            UserStatsTable.createUserStats(userId)
            UserStatsTable.updateGold(userId, 20)
            UserStatsTable.updateMiningExp(userId, 500)
        }

        val stats = getUserStats(userId)

        assertEquals(20, stats.gold)
        assertEquals(500, stats.miningExp)
    }
}
