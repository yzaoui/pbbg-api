package com.bitwiserain.pbbg.test.unit.db.usecase

import com.bitwiserain.pbbg.SchemaHelper
import com.bitwiserain.pbbg.db.repository.SquadTableImpl
import com.bitwiserain.pbbg.db.repository.UnitTable.UnitForm
import com.bitwiserain.pbbg.db.repository.UnitTableImpl
import com.bitwiserain.pbbg.db.repository.UserTableImpl
import com.bitwiserain.pbbg.db.repository.battle.BattleSessionTableImpl
import com.bitwiserain.pbbg.db.usecase.UnitUCImpl
import com.bitwiserain.pbbg.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.domain.model.Squad
import com.bitwiserain.pbbg.domain.usecase.SquadInBattleException
import com.bitwiserain.pbbg.domain.usecase.UnitUC
import com.bitwiserain.pbbg.test.createTestUserAndGetId
import com.bitwiserain.pbbg.test.initDatabase
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class UnitUCImplTests {

    private val db = initDatabase()
    private val battleSessionTable = BattleSessionTableImpl()
    private val squadTable = SquadTableImpl()
    private val unitTable = UnitTableImpl()
    private val userTable = UserTableImpl()
    private val unitUC: UnitUC = UnitUCImpl(db, battleSessionTable, squadTable, unitTable)

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(db)
    }

    @Test
    fun `When calling getSquad(), should return the user's squad`() {
        val userId = createTestUserAndGetId(db, userTable)

        val units = createUnitsAndSquad(userId)

        val expectedSquad = Squad(units)
        val actualSquad = unitUC.getSquad(userId)

        // TODO: Better assertions, not testing equality here
        assertEquals(expectedSquad.units.size, actualSquad.units.size)
    }

    @Test
    fun `Given a damaged squad out of battle, when healing it, all units should be fully healed`() {
        val userId = createTestUserAndGetId(db, userTable)

        val units = createUnitsAndSquad(userId)

        val healedUnits = transaction(db) {
            // Damage all units
            units.forEach { unitTable.updateUnit(it.id, it.receiveDamage(4).updatedUnit) }

            unitUC.healSquad(userId)

            return@transaction squadTable.getAllies(userId)
        }

        assertTrue(healedUnits.all { it.hp == it.maxHP }, "All units should be fully healed after healSquad().")
    }

    @Test
    fun `Given a user in battle, when healing squad, SquadInBattleException should be thrown`() {
        val userId = createTestUserAndGetId(db, userTable)

        transaction {
            battleSessionTable.createBattleSessionAndGetId(userId)
        }

        assertThrows<SquadInBattleException> {
            unitUC.healSquad(userId)
        }
    }

    private fun createUnitsAndSquad(userId: Int) = transaction(db) {
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
