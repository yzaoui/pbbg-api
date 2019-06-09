package com.bitwiserain.pbbg.test.db.usecase

import com.bitwiserain.pbbg.db.form.MyUnitForm
import com.bitwiserain.pbbg.db.repository.SquadTable
import com.bitwiserain.pbbg.db.repository.UnitTable
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.db.usecase.BattleUCImpl
import com.bitwiserain.pbbg.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.domain.usecase.BattleAlreadyInProgressException
import com.bitwiserain.pbbg.domain.usecase.BattleUC
import com.bitwiserain.pbbg.domain.usecase.NoAlliesAliveException
import com.bitwiserain.pbbg.test.dropDatabase
import com.bitwiserain.pbbg.test.initDatabase
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class BattleUCImplTests {
    private val db = initDatabase()
    private val battleUC: BattleUC = BattleUCImpl(db)

    @AfterEach
    fun dropDatabase() {
        dropDatabase(db)
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
        fun `Given a user who is already in a battle, when they generate a new battle, a BattleAlreadyInProgressException should be thrown`() {
            val userId = createTestUserAndGetId(db)

            // Give user a squad
            insertAndGetAllies(userId)

            // Generate a battle
            battleUC.generateBattle(userId.value)

            // Attempt to generate another battle while the first is in progress
            assertFailsWith(BattleAlreadyInProgressException::class) {
                battleUC.generateBattle(userId.value)
            }
        }

        @Test
        fun `Given a user with no living units in squad, when they generate a new battle, a NoAlliesAliveException should be thrown`() {
            val userId = createTestUserAndGetId(db)

            transaction(db) {
                SquadTable.insertAllies(userId, listOf(
                    MyUnitForm(MyUnitEnum.ICE_CREAM_WIZARD, 9, 1)
                ))
                val allies = SquadTable.getAllies(userId.value)

                // Kill the only unit in squad
                UnitTable.updateUnit(allies[0].id, allies[0].receiveDamage(allies[0].hp))
            }

            // Attempt to generate battle with a wiped out squad
            assertFailsWith(NoAlliesAliveException::class) {
                battleUC.generateBattle(userId.value)
            }
        }
    }

    @Test
    fun `Given a user who hasn't started a battle, when their current battle is requested, null should be returned`() {
        val userId = createTestUserAndGetId(db).value

        val battle = battleUC.getCurrentBattle(userId)

        assertEquals(battle, null, "User should not have a battle in session without requesting one.")
    }

    private fun createTestUserAndGetId(db: Database): EntityID<Int> = transaction(db) {
        UserTable.createUserAndGetId("testuser", ByteArray(60))
    }

    private fun insertAndGetAllies(userId: EntityID<Int>) = transaction(db) {
        SquadTable.insertAllies(userId, listOf(
            MyUnitForm(MyUnitEnum.ICE_CREAM_WIZARD, 9, 1),
            MyUnitForm(MyUnitEnum.CARPSHOOTER, 8, 1),
            MyUnitForm(MyUnitEnum.TWOLIP, 11, 2)
        ))
        SquadTable.getAllies(userId.value)
    }
}
