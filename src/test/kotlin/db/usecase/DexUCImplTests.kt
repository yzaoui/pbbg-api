package com.bitwiserain.pbbg.test.db.usecase

import com.bitwiserain.pbbg.SchemaHelper
import com.bitwiserain.pbbg.db.repository.DexTable
import com.bitwiserain.pbbg.db.usecase.DexUCImpl
import com.bitwiserain.pbbg.domain.model.BaseItem
import com.bitwiserain.pbbg.domain.model.ItemEnum
import com.bitwiserain.pbbg.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.domain.usecase.DexUC
import com.bitwiserain.pbbg.domain.usecase.InvalidItemException
import com.bitwiserain.pbbg.domain.usecase.InvalidUnitException
import com.bitwiserain.pbbg.domain.usecase.ItemUndiscoveredException
import com.bitwiserain.pbbg.test.createTestUserAndGetId
import com.bitwiserain.pbbg.test.initDatabase
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertFalse
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class DexUCImplTests {
    private val db = initDatabase()
    private val dexUC: DexUC = DexUCImpl(db)

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(db)
    }

    @Nested
    inner class DexItems {
        @Test
        fun `Given a user who's discovered some items, when calling for dex items, those exact ones should return`() {
            val userId = createTestUserAndGetId(db)

            val discoveredItemEnums = setOf(ItemEnum.COPPER_ORE, ItemEnum.ICE_PICK)

            transaction(db) {
                DexTable.insertDiscovered(userId, discoveredItemEnums)
            }

            val dexItems = dexUC.getDexItems(userId)

            assertEquals(discoveredItemEnums, dexItems.discoveredItems)
        }

        @Test
        fun `When calling for dex items, lastItemId should be 11`() {
            val userId = createTestUserAndGetId(db)

            val dexItems = dexUC.getDexItems(userId)

            assertEquals(11, dexItems.lastItemId)
        }
    }

    @Nested
    inner class IndividualDexItem {
        @Test
        fun `Given a user who has discovered an item, when calling for said item by its ID, it should be returned in detail`() {
            val userId = createTestUserAndGetId(db)

            val discoveredItem = BaseItem.Material.Coal

            transaction(db) {
                DexTable.insertDiscovered(userId, discoveredItem.enum)
            }

            val dexItem = dexUC.getIndividualDexBaseItem(userId, discoveredItem.id)

            assertEquals(dexItem, discoveredItem)
        }

        @Test
        fun `Given a user, when calling for an invalid item by ID, an InvalidItemException should be thrown`() {
            val userId = createTestUserAndGetId(db)

            assertFailsWith<InvalidItemException> {
                dexUC.getIndividualDexBaseItem(userId, ItemEnum.values().size + 7)
            }
        }

        @Test
        fun `Given a user who hasn't discovered an item, when calling for said item by its ID, an ItemUndiscoveredException should be thrown`() {
            val userId = createTestUserAndGetId(db)

            assertFailsWith<ItemUndiscoveredException> {
                dexUC.getIndividualDexBaseItem(userId, BaseItem.Material.CopperOre.id)
            }
        }
    }

    @Nested
    inner class DexUnits {
        @Test
        fun `When calling getDexUnits(), all dex units should be returned`() {
            val userId = createTestUserAndGetId(db)

            val dexUnits = dexUC.getDexUnits(userId)

            assertEquals(MyUnitEnum.values().toSet(), dexUnits.discoveredUnits)
        }
    }

    @Nested
    inner class IndividualDexUnit {
        @Test
        fun `Given a dex unit ID, when calling getDexUnit(), that dex unit should be returned`() {
            val userId = createTestUserAndGetId(db)

            val dexUnit = dexUC.getDexUnit(userId, 2)

            assertEquals(MyUnitEnum.values()[1], dexUnit)
        }

        @Test
        fun `Given an invalid dex unit ID, when calling getDexUnit(), InvalidUnitException should be thrown`() {
            val userId = createTestUserAndGetId(db)

            assertThrows<InvalidUnitException> {
                dexUC.getDexUnit(userId, 5000)
            }
        }
    }
}
