package com.bitwiserain.pbbg.app.test.unit.db.usecase

import com.bitwiserain.pbbg.app.db.repository.SquadTable
import com.bitwiserain.pbbg.app.db.repository.battle.BattleEnemyTable
import com.bitwiserain.pbbg.app.db.repository.battle.BattleSessionTable
import com.bitwiserain.pbbg.app.db.usecase.GetBattleUCImpl
import com.bitwiserain.pbbg.app.test.db.TestTransaction
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class GetBattleUCImplTest {

    private val userId: Int = 1234
    private val battleSessionNotNull = 123L
    private var battleSession: Long? = null

    private val battleEnemyTable: BattleEnemyTable = mockk {
        every { getEnemies(battleSessionNotNull) } returns mockk()
    }
    private val battleSessionTable: BattleSessionTable = mockk {
        every { getBattleSessionId(userId) } answers { battleSession }
        every { getBattleQueue(battleSessionNotNull) } returns mockk()
    }
    private val squadTable: SquadTable = mockk {
        every { getAllies(userId) } returns mockk()
    }

    private val getBattle: GetBattleUCImpl = GetBattleUCImpl(TestTransaction, battleEnemyTable, battleSessionTable, squadTable)

    @Test
    @DisplayName("When generating a battle and requesting the current battle, it should be returned")
    fun getBattleNotNull() {
        battleSession = battleSessionNotNull

        val battle = getBattle(userId)

        assertNotNull(battle, "User should have a battle in session.")
    }

    @Test
    @DisplayName("Given an out-of-battle user, when their current battle is requested, null should be returned")
    fun getBattleNull() {
        battleSession = null

        val battle = getBattle(userId)

        assertNull(battle, "User should not have a battle in session without requesting one.")
    }
}
