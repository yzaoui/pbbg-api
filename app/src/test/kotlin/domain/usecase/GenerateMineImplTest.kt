package com.bitwiserain.pbbg.app.test.domain.usecase

import com.bitwiserain.pbbg.app.db.repository.UserStatsTable
import com.bitwiserain.pbbg.app.db.repository.mine.MineCellTable
import com.bitwiserain.pbbg.app.db.repository.mine.MineSessionTable
import com.bitwiserain.pbbg.app.domain.model.mine.MineType
import com.bitwiserain.pbbg.app.domain.usecase.mine.GenerateMine.Result
import com.bitwiserain.pbbg.app.domain.usecase.mine.GenerateMineImpl
import com.bitwiserain.pbbg.app.test.db.TestTransaction
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class GenerateMineImplTest {

    private val userId = 1234

    private val mineCellTable: MineCellTable = mockk(relaxUnitFun = true)
    private val mineSessionTable: MineSessionTable = mockk(relaxUnitFun = true)
    private val userStatsTable: UserStatsTable = mockk(relaxUnitFun = true)

    private val generateMine: GenerateMineImpl = GenerateMineImpl(TestTransaction, mineCellTable, mineSessionTable, userStatsTable)

    @Test
    @DisplayName("Given user is already in a mine, then should return AlreadyInMine")
    fun alreadyInMine() {
        // User is in a mine session
        every { mineSessionTable.getSession(userId) } returns mockk()

        val result = generateMine(userId, mineTypeId = 0, width = 0, height = 0)

        result shouldBe Result.AlreadyInMine
    }

    @Test
    @DisplayName("Given user is not in a mine, when requesting an invalid mine type, then should return InvalidMineTypeId")
    fun invalidMineTypeId() {
        // User is not in a mine session
        every { mineSessionTable.getSession(userId) } returns null
        val invalidMineType = 2000.also { check(it !in MineType.entries.indices) }

        val result = generateMine(userId, invalidMineType, width = 10, height = 10)

        result shouldBe Result.InvalidMineTypeId
    }

    // TODO: More tests
}
