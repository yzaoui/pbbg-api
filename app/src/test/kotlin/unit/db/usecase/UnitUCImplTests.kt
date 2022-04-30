package com.bitwiserain.pbbg.app.test.unit.db.usecase

import com.bitwiserain.pbbg.app.SchemaHelper
import com.bitwiserain.pbbg.app.db.repository.SquadTableImpl
import com.bitwiserain.pbbg.app.db.repository.UnitTable.UnitForm
import com.bitwiserain.pbbg.app.db.repository.UnitTableImpl
import com.bitwiserain.pbbg.app.db.repository.UserTableImpl
import com.bitwiserain.pbbg.app.db.repository.battle.BattleSessionTableImpl
import com.bitwiserain.pbbg.app.db.usecase.UnitUCImpl
import com.bitwiserain.pbbg.app.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.app.domain.model.Squad
import com.bitwiserain.pbbg.app.domain.usecase.SquadInBattleException
import com.bitwiserain.pbbg.app.domain.usecase.UnitUC
import com.bitwiserain.pbbg.app.test.createTestUserAndGetId
import com.bitwiserain.pbbg.app.test.initDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class UnitUCImplTests {

    private val transaction = initDatabase()
    private val battleSessionTable = BattleSessionTableImpl()
    private val squadTable = SquadTableImpl()
    private val unitTable = UnitTableImpl()
    private val userTable = UserTableImpl()
    private val unitUC: UnitUC = UnitUCImpl(transaction, battleSessionTable, squadTable, unitTable)

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(transaction)
    }

    @Test
    fun `When calling getSquad(), should return the user's squad`() {
        val userId = createTestUserAndGetId(transaction, userTable)

        val units = createUnitsAndSquad(userId)

        val expectedSquad = Squad(units)
        val actualSquad = unitUC.getSquad(userId)

        // TODO: Better assertions, not testing equality here
        assertEquals(expectedSquad.units.size, actualSquad.units.size)
    }

    @Test
    fun `Given a damaged squad out of battle, when healing it, all units should be fully healed`() {
        val userId = createTestUserAndGetId(transaction, userTable)

        val units = createUnitsAndSquad(userId)

        val healedUnits = transaction {
            // Damage all units
            units.forEach { unitTable.updateUnit(it.id, it.receiveDamage(4).updatedUnit) }

            unitUC.healSquad(userId)

            return@transaction squadTable.getAllies(userId)
        }

        assertTrue(healedUnits.all { it.hp == it.maxHP }, "All units should be fully healed after healSquad().")
    }

    @Test
    fun `Given a user in battle, when healing squad, SquadInBattleException should be thrown`() {
        val userId = createTestUserAndGetId(transaction, userTable)

        transaction {
            battleSessionTable.createBattleSessionAndGetId(userId)
        }

        assertThrows<SquadInBattleException> {
            unitUC.healSquad(userId)
        }
    }

    private fun createUnitsAndSquad(userId: Int) = transaction {
        listOf(
            UnitForm(MyUnitEnum.ICE_CREAM_WIZARD, 9, 1, 1, 1, 1),
            UnitForm(MyUnitEnum.CARPSHOOTER, 8, 1, 2, 1, 1),
            UnitForm(MyUnitEnum.TWOLIP, 11, 2, 1, 1, 1)
        )
            .map { unitTable.insertUnitAndGetId(it) }
            .also { squadTable.insertUnits(userId, it) }
            .map { unitTable.getUnit(it)!! }
    }
}
