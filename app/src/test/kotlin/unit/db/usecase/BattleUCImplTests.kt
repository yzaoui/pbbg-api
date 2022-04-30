package com.bitwiserain.pbbg.app.test.unit.db.usecase

import com.bitwiserain.pbbg.app.SchemaHelper
import com.bitwiserain.pbbg.app.db.repository.SquadTableImpl
import com.bitwiserain.pbbg.app.db.repository.UnitTable.UnitForm
import com.bitwiserain.pbbg.app.db.repository.UnitTableImpl
import com.bitwiserain.pbbg.app.db.repository.UserTableImpl
import com.bitwiserain.pbbg.app.db.repository.battle.BattleEnemyTableImpl
import com.bitwiserain.pbbg.app.db.repository.battle.BattleSessionTableImpl
import com.bitwiserain.pbbg.app.db.usecase.BattleUCImpl
import com.bitwiserain.pbbg.app.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.app.domain.usecase.BattleAlreadyInProgressException
import com.bitwiserain.pbbg.app.domain.usecase.BattleUC
import com.bitwiserain.pbbg.app.domain.usecase.NoAlliesAliveException
import com.bitwiserain.pbbg.app.test.createTestUserAndGetId
import com.bitwiserain.pbbg.app.test.initDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class BattleUCImplTests {

    private val transaction = initDatabase()
    private val battleEnemyTable = BattleEnemyTableImpl()
    private val battleSessionTable = BattleSessionTableImpl()
    private val squadTable = SquadTableImpl()
    private val unitTable = UnitTableImpl()
    private val userTable = UserTableImpl()
    private val battleUC: BattleUC = BattleUCImpl(transaction, battleEnemyTable, battleSessionTable, squadTable, unitTable)

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(transaction)
    }

    @Nested
    inner class BattleGeneration {
        @Test
        fun `Given an out-of-battle user, when generating a new battle, a battle containing the squad and 1+ enemies should return`() {
            val userId = createTestUserAndGetId(transaction, userTable)

            val allies = insertAndGetAllies(userId)

            val battle = battleUC.generateBattle(userId)

            assertFalse(battle.allies.isEmpty(), "There should be allies in the battle.")
            assertFalse(battle.enemies.isEmpty(), "There should be enemies in the battle.")
            assertEquals(battle.allies, allies, "The allies in the battle should be the squad in place when the battle was generated.")
        }

        @Test
        fun `Given an out-of-battle user, when generating a battle, its battle queue should include every ally and enemy exactly once`() {
            val userId = createTestUserAndGetId(transaction, userTable).also {
                insertAndGetAllies(it)
            }

            val battle = battleUC.generateBattle(userId)

            val queueIds = battle.battleQueue.turns.map { it.unitId }.sorted()
            val unitIds = battle.run { allies + enemies }.map { it.id }.sorted()

            assertEquals(queueIds, unitIds, "Every unit in the battle should be in the battle queue exactly once.")
        }

        @Test
        fun `Given an in-battle user, when generating a new battle, BattleAlreadyInProgressException should be thrown`() {
            val userId = createTestUserAndGetId(transaction, userTable)

            // Give user a squad
            insertAndGetAllies(userId)

            // Generate a battle
            battleUC.generateBattle(userId)

            // Attempt to generate another battle while the first is in progress
            assertFailsWith<BattleAlreadyInProgressException>("Generating a new battle should fail if one is currently in progress") {
                battleUC.generateBattle(userId)
            }
        }

        @Test
        fun `Given a user with only dead units, when generating a new battle, NoAlliesAliveException should be thrown`() {
            val userId = createTestUserAndGetId(transaction, userTable)

            transaction {
                listOf(UnitForm(MyUnitEnum.ICE_CREAM_WIZARD, 9, 1, 1, 1, 1))
                    .map { unitTable.insertUnitAndGetId(it) }
                    .also { squadTable.insertUnits(userId, it) }
                val allies = squadTable.getAllies(userId)

                // Kill the only unit in squad
                unitTable.updateUnit(allies[0].id, allies[0].receiveDamage(allies[0].hp).updatedUnit)
            }

            // Attempt to generate battle with a wiped out squad
            assertFailsWith<NoAlliesAliveException> ("Generating a new battle should fail if user's squad is wiped out") {
                battleUC.generateBattle(userId)
            }
        }
    }

    @Nested
    inner class BattleRetrieval {
        @Test
        fun `When generating a battle and requesting the current battle, it should be returned`() {
            val userId = createTestUserAndGetId(transaction, userTable)
            insertAndGetAllies(userId)
            battleUC.generateBattle(userId)

            val battle = battleUC.getCurrentBattle(userId)

            assertNotNull(battle, "User should have a battle in session.")
        }

        @Test
        fun `Given an out-of-battle user, when their current battle is requested, null should be returned`() {
            val userId = createTestUserAndGetId(transaction, userTable)

            val battle = battleUC.getCurrentBattle(userId)

            assertNull(battle, "User should not have a battle in session without requesting one.")
        }
    }

    private fun insertAndGetAllies(userId: Int) = transaction {
        listOf(
            UnitForm(MyUnitEnum.ICE_CREAM_WIZARD, 9, 1, 2, 1, 1),
            UnitForm(MyUnitEnum.CARPSHOOTER, 8, 1, 1, 1, 1),
            UnitForm(MyUnitEnum.TWOLIP, 11, 2, 1, 1, 1)
        ).map {
            unitTable.insertUnitAndGetId(it)
        }.also {
            squadTable.insertUnits(userId, it)
        }

        return@transaction squadTable.getAllies(userId)
    }
}
