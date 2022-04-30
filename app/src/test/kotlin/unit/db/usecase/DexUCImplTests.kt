package com.bitwiserain.pbbg.app.test.unit.db.usecase

import com.bitwiserain.pbbg.app.SchemaHelper
import com.bitwiserain.pbbg.app.db.repository.DexTableImpl
import com.bitwiserain.pbbg.app.db.repository.UserTableImpl
import com.bitwiserain.pbbg.app.db.usecase.DexUCImpl
import com.bitwiserain.pbbg.app.domain.model.BaseItem
import com.bitwiserain.pbbg.app.domain.model.ItemEnum
import com.bitwiserain.pbbg.app.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.app.domain.model.dex.DexItem
import com.bitwiserain.pbbg.app.domain.usecase.DexUC
import com.bitwiserain.pbbg.app.domain.usecase.InvalidItemException
import com.bitwiserain.pbbg.app.domain.usecase.InvalidUnitException
import com.bitwiserain.pbbg.app.test.createTestUserAndGetId
import com.bitwiserain.pbbg.app.test.initDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class DexUCImplTests {

    private val transaction = initDatabase()
    private val dexTable = DexTableImpl()
    private val userTable = UserTableImpl()
    private val dexUC: DexUC = DexUCImpl(transaction, dexTable)

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(transaction)
    }

    @Nested
    inner class DexItems {
        @Test
        fun `Given a user who's discovered some items, when calling for dex items, those exact ones should return`() {
            val userId = createTestUserAndGetId(transaction, userTable)

            val discoveredItemEnums = setOf(ItemEnum.COPPER_ORE, ItemEnum.ICE_PICK)

            transaction {
                dexTable.insertDiscovered(userId, discoveredItemEnums)
            }

            val dexItems = dexUC.getDexItems(userId)

            assertEquals(discoveredItemEnums, dexItems.discoveredItems)
        }

        @Test
        fun `When calling for dex items, lastItemId should be 11`() {
            val userId = createTestUserAndGetId(transaction, userTable)

            val dexItems = dexUC.getDexItems(userId)

            assertEquals(11, dexItems.lastItemId)
        }
    }

    @Nested
    inner class IndividualDexItem {
        @Test
        fun `Given a user who has discovered an item, when calling for said item by its ID, it should be returned in detail`() {
            val userId = createTestUserAndGetId(transaction, userTable)

            val discoveredItem = BaseItem.Material.Coal

            transaction {
                dexTable.insertDiscovered(userId, discoveredItem.enum)
            }

            val dexItem = dexUC.getIndividualDexBaseItem(userId, discoveredItem.id)

            assertTrue(dexItem is DexItem.DiscoveredDexItem)
            assertEquals(dexItem.baseItem, discoveredItem)
        }

        @Test
        fun `Given a user, when calling for an invalid item by ID, an InvalidItemException should be thrown`() {
            val userId = createTestUserAndGetId(transaction, userTable)

            assertFailsWith<InvalidItemException> {
                dexUC.getIndividualDexBaseItem(userId, ItemEnum.values().size + 7)
            }
        }

        @Test
        fun `Given a user who hasn't discovered an item, when calling for said item by its ID, it should be undiscovered`() {
            val userId = createTestUserAndGetId(transaction, userTable)

            val dexItem = dexUC.getIndividualDexBaseItem(userId, BaseItem.Material.CopperOre.id)

            assertTrue(dexItem is DexItem.UndiscoveredDexItem)
            assertEquals(BaseItem.Material.CopperOre.id, dexItem.id)
        }
    }

    @Nested
    inner class DexUnits {
        @Test
        fun `When calling getDexUnits(), all dex units should be returned`() {
            val userId = createTestUserAndGetId(transaction, userTable)

            val dexUnits = dexUC.getDexUnits(userId)

            assertEquals(MyUnitEnum.values().toSet(), dexUnits.discoveredUnits)
        }
    }

    @Nested
    inner class IndividualDexUnit {
        @Test
        fun `Given a dex unit ID, when calling getDexUnit(), that dex unit should be returned`() {
            val userId = createTestUserAndGetId(transaction, userTable)

            val dexUnit = dexUC.getDexUnit(userId, 2)

            assertEquals(MyUnitEnum.values()[1], dexUnit)
        }

        @Test
        fun `Given an invalid dex unit ID, when calling getDexUnit(), InvalidUnitException should be thrown`() {
            val userId = createTestUserAndGetId(transaction, userTable)

            assertThrows<InvalidUnitException> {
                dexUC.getDexUnit(userId, 5000)
            }
        }
    }
}
