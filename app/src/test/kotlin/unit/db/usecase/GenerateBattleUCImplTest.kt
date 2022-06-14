package com.bitwiserain.pbbg.app.test.unit.db.usecase

import com.bitwiserain.pbbg.app.db.repository.SquadTable
import com.bitwiserain.pbbg.app.db.repository.UnitTable
import com.bitwiserain.pbbg.app.db.repository.battle.BattleEnemyTable
import com.bitwiserain.pbbg.app.db.repository.battle.BattleSessionTable
import com.bitwiserain.pbbg.app.db.usecase.GenerateBattleUCImpl
import com.bitwiserain.pbbg.app.domain.model.MyUnit
import com.bitwiserain.pbbg.app.domain.usecase.BattleAlreadyInProgressException
import com.bitwiserain.pbbg.app.domain.usecase.NoAlliesAliveException
import com.bitwiserain.pbbg.app.test.db.TestTransaction
import com.bitwiserain.pbbg.app.test.db.repository.BattleEnemyTableTestImpl
import com.bitwiserain.pbbg.app.test.db.repository.UnitTableTestImpl
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class GenerateBattleUCImplTest {

    private val units = mutableMapOf<Long, MyUnit>()

    private val userId: Int = 1234
    private val newBattleId: Long = 123L
    private val aliveAlies: List<MyUnit> = listOf(
        mockk {
            every { alive } returns true
            every { id } returns 7L
        }
    )
    private val deadAlies: List<MyUnit> = listOf(
        mockk {
            every { alive } returns false
            every { id } returns 7L
        }
    )

    private val battleEnemyTable: BattleEnemyTable = BattleEnemyTableTestImpl(units = units)
    private val battleSessionTable: BattleSessionTable = mockk(relaxUnitFun = true)
    private val squadTable: SquadTable = mockk()
    private val unitTable: UnitTable = UnitTableTestImpl(units)

    private val generateBattle = GenerateBattleUCImpl(TestTransaction, battleEnemyTable, battleSessionTable, squadTable, unitTable)

    @Test
    fun `Given an out-of-battle user, when generating a new battle, a battle containing the squad and 1+ enemies should return`() {
        with(battleSessionTable) {
            every { isBattleInProgress(userId) } returns false
            every { createBattleSessionAndGetId(userId) } returns newBattleId
        }
        every { squadTable.getAllies(userId) } returns aliveAlies

        val battle = generateBattle(userId)

        assertFalse(battle.allies.isEmpty(), "There should be allies in the battle.")
        assertFalse(battle.enemies.isEmpty(), "There should be enemies in the battle.")
        assertEquals(battle.allies, aliveAlies, "The allies in the battle should be the squad in place when the battle was generated.")
    }

    @Test
    fun `Given an out-of-battle user, when generating a battle, its battle queue should include every ally and enemy exactly once`() {
        with(battleSessionTable) {
            every { isBattleInProgress(userId) } returns false
            every { createBattleSessionAndGetId(userId) } returns newBattleId
        }
        every { squadTable.getAllies(userId) } returns aliveAlies

        val battle = generateBattle(userId)

        val queueIds = battle.battleQueue.turns.map { it.unitId }.sorted()
        val unitIds = battle.run { allies + enemies }.map { it.id }.sorted()

        assertEquals(queueIds, unitIds, "Every unit in the battle should be in the battle queue exactly once.")
    }

    @Test
    fun `Given an in-battle user, when generating a new battle, BattleAlreadyInProgressException should be thrown`() {
        with(battleSessionTable) {
            every { isBattleInProgress(userId) } returns false
            every { createBattleSessionAndGetId(userId) } returns newBattleId
        }
        every { squadTable.getAllies(userId) } returns aliveAlies

        // Generate a battle
        generateBattle(userId)

        // Attempt to generate another battle while the first is in progress
        every { battleSessionTable.isBattleInProgress(userId) } returns true
        assertFailsWith<BattleAlreadyInProgressException>("Generating a new battle should fail if one is currently in progress") {
            generateBattle(userId)
        }
    }

    @Test
    fun `Given an out-of-battle user with only dead units, when generating a new battle, NoAlliesAliveException should be thrown`() {
        every { battleSessionTable.isBattleInProgress(userId) } returns false
        every { squadTable.getAllies(userId) } returns deadAlies

        // Attempt to generate battle with a wiped out squad
        assertFailsWith<NoAlliesAliveException> ("Generating a new battle should fail if user's squad is wiped out") {
            generateBattle(userId)
        }
    }
}
