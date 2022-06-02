package com.bitwiserain.pbbg.app.test.domain.usecase

import com.bitwiserain.pbbg.app.SchemaHelper
import com.bitwiserain.pbbg.app.db.repository.UserStatsTableImpl
import com.bitwiserain.pbbg.app.db.repository.UserTableImpl
import com.bitwiserain.pbbg.app.domain.usecase.GetUserStatsUCImpl
import com.bitwiserain.pbbg.app.test.createTestUserAndGetId
import com.bitwiserain.pbbg.app.test.initDatabase
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class GetUserStatsUCImplTest {

    private val transaction = initDatabase()
    private val userTable = UserTableImpl()
    private val userStatsTable = UserStatsTableImpl()
    private val getUserStats = GetUserStatsUCImpl(transaction, userStatsTable)

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(transaction)
    }

    @Test
    fun `When getting user stats by ID, the user stats should return`() {
        val userId = createTestUserAndGetId(transaction, userTable)

        transaction {
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
