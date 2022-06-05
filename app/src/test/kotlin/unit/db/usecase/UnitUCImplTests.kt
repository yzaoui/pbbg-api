package com.bitwiserain.pbbg.app.test.unit.db.usecase

import com.bitwiserain.pbbg.app.db.repository.SquadTable
import com.bitwiserain.pbbg.app.db.repository.UnitTable
import com.bitwiserain.pbbg.app.db.repository.battle.BattleSessionTable
import com.bitwiserain.pbbg.app.db.usecase.UnitUCImpl
import com.bitwiserain.pbbg.app.domain.model.MyUnit
import com.bitwiserain.pbbg.app.domain.model.Squad
import com.bitwiserain.pbbg.app.domain.usecase.SquadInBattleException
import com.bitwiserain.pbbg.app.test.db.TestTransaction
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class UnitUCImplTests {

    val userId: Int = 1234

    private val battleSessionTable: BattleSessionTable = mockk()
    private val squadTable: SquadTable = mockk()
    private val unitTable: UnitTable = mockk(relaxUnitFun = true)

    private val unitUC: UnitUCImpl = UnitUCImpl(TestTransaction, battleSessionTable, squadTable, unitTable)

    @Test
    fun `When calling getSquad(), should return the user's squad`() {
        val expectedAllies = listOf(
            MyUnit.IceCreamWizard(0L, 9, 9, 1, 1, 1, 1, 0L),
            MyUnit.Carpshooter(1L, 8, 8, 1, 2, 1, 1, 0L),
            MyUnit.Twolip(2L, 11, 11, 2, 1, 1, 1, 0L)
        )
        every { squadTable.getAllies(userId) } returns expectedAllies

        val expectedSquad = Squad(expectedAllies)
        val actualSquad = unitUC.getSquad(userId)

        // TODO: Better assertions, not testing equality here
        assertEquals(expectedSquad.units.size, actualSquad.units.size)
    }

    @Test
    fun `Given a damaged squad out of battle, when healing it, all units should be fully healed`() {
        every { battleSessionTable.isBattleInProgress(userId) } returns false
        val expectedAllies = listOf(
            MyUnit.IceCreamWizard(0L, 2, 9, 1, 1, 1, 1, 0L),
            MyUnit.Carpshooter(1L, 4, 8, 1, 2, 1, 1, 0L),
            MyUnit.Twolip(2L, 0, 11, 2, 1, 1, 1, 0L)
        )
        // Squad is not fully healed
        assert(expectedAllies.any { it.hp < it.maxHP })
        every { squadTable.getAllies(userId) } returns expectedAllies

        unitUC.healSquad(userId)

        verifyAll {
            expectedAllies.filter { it.hp < it.maxHP }
                // Every damaged unit should get healed to max
                .forEach { unitTable.updateUnit(it.id, it.maxHeal()) }
        }
    }

    @Test
    fun `Given a user in battle, when healing squad, SquadInBattleException should be thrown`() {
        every { battleSessionTable.isBattleInProgress(userId) } returns true

        assertThrows<SquadInBattleException> {
            unitUC.healSquad(userId)
        }
    }
}
