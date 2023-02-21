package com.bitwiserain.pbbg.app.test.domain.usecase

import com.bitwiserain.pbbg.app.db.repository.mine.MineCellTable
import com.bitwiserain.pbbg.app.db.repository.mine.MineSessionTable
import com.bitwiserain.pbbg.app.domain.usecase.mine.GetMineImpl
import com.bitwiserain.pbbg.app.test.db.TestTransaction
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class GetMineImplTest {

    private val userId = 1234

    private val mineCellTable: MineCellTable = mockk(relaxUnitFun = true)
    private val mineSessionTable: MineSessionTable = mockk(relaxUnitFun = true)

    private val getMine: GetMineImpl = GetMineImpl(TestTransaction, mineCellTable, mineSessionTable)

    @Test
    @DisplayName("Given user is not in a mine, then should return null")
    fun mineSessionNull() {
        // User is not in a mine session
        every { mineSessionTable.getSession(userId) } returns null

        val result = getMine(userId)

        result shouldBe null
    }

    @Test
    @DisplayName("Given user is in a mine, then should return a mine")
    @Disabled
    fun mineSessionNotNull() {
        // TODO: Implement test
    }
}
