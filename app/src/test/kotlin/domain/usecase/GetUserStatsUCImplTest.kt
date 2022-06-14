package com.bitwiserain.pbbg.app.test.domain.usecase

import com.bitwiserain.pbbg.app.db.repository.UserStatsTable
import com.bitwiserain.pbbg.app.domain.model.UserStats
import com.bitwiserain.pbbg.app.domain.usecase.GetUserStatsUCImpl
import com.bitwiserain.pbbg.app.test.db.TestTransaction
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class GetUserStatsUCImplTest {

    val userId = 1234

    private val userStatsTable: UserStatsTable = mockk()

    private val getUserStats: GetUserStatsUCImpl = GetUserStatsUCImpl(TestTransaction, userStatsTable)

    @Test
    fun `When getting user stats by ID, the user stats should return`() {
        val expectedUserStats: UserStats = mockk()
        every { userStatsTable.getUserStats(userId) } returns expectedUserStats

        getUserStats(userId) shouldBe expectedUserStats
    }
}
