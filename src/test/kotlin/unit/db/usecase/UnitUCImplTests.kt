package com.bitwiserain.pbbg.test.unit.db.usecase

import com.bitwiserain.pbbg.SchemaHelper
import com.bitwiserain.pbbg.db.repository.SquadTable
import com.bitwiserain.pbbg.db.repository.UnitForm
import com.bitwiserain.pbbg.db.repository.UnitTable
import com.bitwiserain.pbbg.db.repository.battle.BattleSessionTable
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
    private val unitUC: UnitUC = UnitUCImpl(db)

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(db)
    }

    @Test
    fun `When calling getSquad(), should return the user's squad`() {
        val userId = createTestUserAndGetId(db)

        val units = createUnitsAndSquad(userId)

        val expectedSquad = Squad(units)
        val actualSquad = unitUC.getSquad(userId)

        // TODO: Better assertions, not testing equality here
        assertEquals(expectedSquad.units.size, actualSquad.units.size)
    }

    @Test
    fun `Given a damaged squad out of battle, when healing it, all units should be fully healed`() {
        val userId = createTestUserAndGetId(db)

        val units = createUnitsAndSquad(userId)

        val healedUnits = transaction(db) {
            // Damage all units
            units.forEach { UnitTable.updateUnit(it.id, it.receiveDamage(4)) }

            unitUC.healSquad(userId)

            return@transaction SquadTable.getAllies(userId)
        }

        assertTrue(healedUnits.all { it.hp == it.maxHP }, "All units should be fully healed after healSquad().")
    }

    @Test
    fun `Given a user in battle, when healing squad, SquadInBattleException should be thrown`() {
        val userId = createTestUserAndGetId(db)

        transaction {
            BattleSessionTable.createBattleSessionAndGetId(userId)
        }

        assertThrows<SquadInBattleException> {
            unitUC.healSquad(userId)
        }
    }

    private fun createUnitsAndSquad(userId: Int) = transaction(db) {
        transaction(db) {
            listOf(
                UnitForm(MyUnitEnum.ICE_CREAM_WIZARD, 9, 1, 1),
                UnitForm(MyUnitEnum.CARPSHOOTER, 8, 1, 2),
                UnitForm(MyUnitEnum.TWOLIP, 11, 2, 1)
            )
                .map { UnitTable.insertUnitAndGetId(it) }
                .also { SquadTable.insertUnits(userId, it) }
                .map { UnitTable.getUnit(it)!! }
        }
    }
}
