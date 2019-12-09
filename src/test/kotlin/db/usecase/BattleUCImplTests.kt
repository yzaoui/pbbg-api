package com.bitwiserain.pbbg.test.db.usecase

import com.bitwiserain.pbbg.SchemaHelper
import com.bitwiserain.pbbg.db.form.MyUnitForm
import com.bitwiserain.pbbg.db.repository.SquadTable
import com.bitwiserain.pbbg.db.repository.UnitTable
import com.bitwiserain.pbbg.db.usecase.BattleUCImpl
import com.bitwiserain.pbbg.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.domain.usecase.BattleAlreadyInProgressException
import com.bitwiserain.pbbg.domain.usecase.BattleUC
import com.bitwiserain.pbbg.domain.usecase.NoAlliesAliveException
import com.bitwiserain.pbbg.test.createTestUserAndGetId
import com.bitwiserain.pbbg.test.initDatabase
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.*

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class BattleUCImplTests {
    private val db = initDatabase()
    private val battleUC: BattleUC = BattleUCImpl(db)

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(db)
    }

    @Nested
    inner class BattleGeneration {
        @Test
        fun `Given a user with a squad who hasn't started a battle, when they generate a new battle, a battle containing their squad and 1+ enemies should be returned`() {
            val userId = createTestUserAndGetId(db)

            val allies = insertAndGetAllies(userId)

            val battle = battleUC.generateBattle(userId.value)

            assertFalse(battle.allies.isEmpty(), "There should be allies in the battle.")
            assertFalse(battle.enemies.isEmpty(), "There should be enemies in the battle.")
            assertEquals(battle.allies, allies, "The allies in the battle should be the squad in place when the battle was generated.")
        }

        @Test
        fun `Given a newly generated battle, its battle queue should include every ally and enemy exactly once`() {
            val userId = createTestUserAndGetId(db).also {
                insertAndGetAllies(it)
            }

            val battle = battleUC.generateBattle(userId.value)

            val queueIds = battle.battleQueue.turns.map { it.unitId }.sorted()
            val unitIds = battle.run { allies + enemies }.map { it.id }.sorted()

            assertEquals(queueIds, unitIds, "Every unit in the battle should be in the battle queue exactly once.")
        }

        @Test
        fun `Given a user who is already in a battle, when they generate a new battle, a BattleAlreadyInProgressException should be thrown`() {
            val userId = createTestUserAndGetId(db)

            // Give user a squad
            insertAndGetAllies(userId)

            // Generate a battle
            battleUC.generateBattle(userId.value)

            // Attempt to generate another battle while the first is in progress
            assertFailsWith(BattleAlreadyInProgressException::class, "Generating a new battle should fail if one is currently in progress") {
                battleUC.generateBattle(userId.value)
            }
        }

        @Test
        fun `Given a user with no living units in squad, when they generate a new battle, a NoAlliesAliveException should be thrown`() {
            val userId = createTestUserAndGetId(db)

            transaction(db) {
                listOf(MyUnitForm(MyUnitEnum.ICE_CREAM_WIZARD, 9, 1, 1))
                    .map { UnitTable.insertUnitAndGetId(it) }
                    .also { SquadTable.insertUnits(userId, it) }
                val allies = SquadTable.getAllies(userId.value)

                // Kill the only unit in squad
                UnitTable.updateUnit(allies[0].id, allies[0].receiveDamage(allies[0].hp))
            }

            // Attempt to generate battle with a wiped out squad
            assertFailsWith(NoAlliesAliveException::class, "Generating a new battle should fail if user's squad is wiped out") {
                battleUC.generateBattle(userId.value)
            }
        }
    }

    @Nested
    inner class BattleRetrieval {
        @Test
        fun `Given a user, when they generate a battle and request their current battle, it should be returned`() {
            val userId = createTestUserAndGetId(db)
            insertAndGetAllies(userId)
            battleUC.generateBattle(userId.value)

            val battle = battleUC.getCurrentBattle(userId.value)

            assertNotNull(battle, "User should have a battle in session.")
        }

        @Test
        fun `Given a user who hasn't started a battle, when their current battle is requested, null should be returned`() {
            val userId = createTestUserAndGetId(db).value

            val battle = battleUC.getCurrentBattle(userId)

            assertNull(battle, "User should not have a battle in session without requesting one.")
        }
    }

    private fun insertAndGetAllies(userId: EntityID<Int>) = transaction(db) {
        listOf(
            MyUnitForm(MyUnitEnum.ICE_CREAM_WIZARD, 9, 1, 2),
            MyUnitForm(MyUnitEnum.CARPSHOOTER, 8, 1, 1),
            MyUnitForm(MyUnitEnum.TWOLIP, 11, 2, 1)
        ).map {
            UnitTable.insertUnitAndGetId(it)
        }.also {
            SquadTable.insertUnits(userId, it)
        }

        return@transaction SquadTable.getAllies(userId.value)
    }
}
