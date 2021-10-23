package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.SchemaHelper
import com.bitwiserain.pbbg.db.repository.UserStatsTableImpl
import com.bitwiserain.pbbg.test.createTestUserAndGetId
import com.bitwiserain.pbbg.test.initDatabase
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class GetUserStatsUCImplTest {

    private val db = initDatabase()
    private val userStatsTable = UserStatsTableImpl()
    private val getUserStats = GetUserStatsUCImpl(db, userStatsTable)

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(db)
    }

    @Test
    fun `When getting user stats by ID, the user stats should return`() {
        val userId = createTestUserAndGetId(db)

        transaction(db) {
            userStatsTable.createUserStats(userId)
            userStatsTable.updateGold(userId, 20)
            userStatsTable.updateMiningExp(userId, 500)
        }

        val stats = getUserStats(userId)

        assertSoftly(stats) {
            gold shouldBe 20
            miningExp shouldBe 500
        }
    }
}
