package com.bitwiserain.pbbg.test.db.usecase

import com.bitwiserain.pbbg.db.form.MyUnitForm
import com.bitwiserain.pbbg.db.repository.SquadTable
import com.bitwiserain.pbbg.db.repository.UnitTable
import com.bitwiserain.pbbg.db.repository.battle.BattleSessionTable
import com.bitwiserain.pbbg.db.usecase.UnitUCImpl
import com.bitwiserain.pbbg.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.domain.model.Squad
import com.bitwiserain.pbbg.domain.usecase.SquadInBattleException
import com.bitwiserain.pbbg.domain.usecase.UnitUC
import com.bitwiserain.pbbg.test.createTestUserAndGetId
import com.bitwiserain.pbbg.test.dropDatabase
import com.bitwiserain.pbbg.test.initDatabase
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
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
        dropDatabase(db)
    }

    @Test
    fun `When calling getSquad(), should return the user's squad`() {
        val userId = createTestUserAndGetId(db)

        val units = createUnitsAndSquad(userId)

        val expectedSquad = Squad(units)
        val actualSquad = unitUC.getSquad(userId.value)

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

            unitUC.healSquad(userId.value)

            return@transaction SquadTable.getAllies(userId.value)
        }

        assertTrue(healedUnits.all { it.hp == it.maxHP }, "All units should be fully healed after healSquad().")
    }

    @Test
    fun `Given a user in battle, when healing squad, SquadInBattleException should be thrown`() {
        val userId = createTestUserAndGetId(db)

        transaction {
            BattleSessionTable.insert {
                it[BattleSessionTable.userId] = userId
                it[BattleSessionTable.battleQueue] = ""
            }
        }

        assertThrows<SquadInBattleException> {
            unitUC.healSquad(userId.value)
        }
    }

    private fun createUnitsAndSquad(userId: EntityID<Int>) = transaction(db) {
        transaction(db) {
            listOf(
                MyUnitForm(MyUnitEnum.ICE_CREAM_WIZARD, 9, 1, 1),
                MyUnitForm(MyUnitEnum.CARPSHOOTER, 8, 1, 2),
                MyUnitForm(MyUnitEnum.TWOLIP, 11, 2, 1)
            ).map {
                UnitTable.insertUnitAndGetId(it)
            }.also {
                SquadTable.insertUnits(userId, it)
            }.map {
                UnitTable.getUnit(it.value)!!
            }
        }
    }
}
