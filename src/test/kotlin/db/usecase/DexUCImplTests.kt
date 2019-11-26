package com.bitwiserain.pbbg.test.db.usecase

import com.bitwiserain.pbbg.db.repository.DexTable
import com.bitwiserain.pbbg.db.usecase.DexUCImpl
import com.bitwiserain.pbbg.domain.model.BaseItem
import com.bitwiserain.pbbg.domain.model.ItemEnum
import com.bitwiserain.pbbg.domain.usecase.DexUC
import com.bitwiserain.pbbg.domain.usecase.InvalidItemException
import com.bitwiserain.pbbg.domain.usecase.ItemUndiscoveredException
import com.bitwiserain.pbbg.test.createTestUserAndGetId
import com.bitwiserain.pbbg.test.initDatabase
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class DexUCImplTests {
    private val db = initDatabase()
    private val dexUC: DexUC = DexUCImpl(db)

    @AfterEach
    fun dropDatabase() {
        com.bitwiserain.pbbg.test.dropDatabase(db)
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

            val dexItems = dexUC.getDexItems(userId.value)

            assertEquals(dexItems.discoveredItems, discoveredItemEnums)
        }

        @Test
        fun `Given a user who hasn't discovered the last item, when calling for dex items, lastItemIsDiscovered should be false`() {
            val userId = createTestUserAndGetId(db)

            val discoveredItemEnums = setOf(ItemEnum.COPPER_ORE, ItemEnum.ICE_PICK).minus(ItemEnum.values().last())

            transaction(db) {
                DexTable.insertDiscovered(userId, discoveredItemEnums)
            }

            val dexItems = dexUC.getDexItems(userId.value)

            assertFalse(dexItems.lastItemIsDiscovered)
        }

        @Test
        fun `Given a user who has discovered the last item, when calling for dex items, lastItemIsDiscovered should be true`() {
            val userId = createTestUserAndGetId(db)

            val discoveredItemEnums = setOf(ItemEnum.COPPER_ORE, ItemEnum.ICE_PICK).plus(ItemEnum.values().last())

            transaction(db) {
                DexTable.insertDiscovered(userId, discoveredItemEnums)
            }

            val dexItems = dexUC.getDexItems(userId.value)

            assertTrue(dexItems.lastItemIsDiscovered)
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

            val dexItem = dexUC.getIndividualDexBaseItem(userId.value, discoveredItem.enum.ordinal)

            assertEquals(dexItem, discoveredItem)
        }

        @Test
        fun `Given a user, when calling for an invalid item by ID, an InvalidItemException should be thrown`() {
            val userId = createTestUserAndGetId(db)

            assertFailsWith<InvalidItemException> {
                dexUC.getIndividualDexBaseItem(userId.value, ItemEnum.values().size + 7)
            }
        }

        @Test
        fun `Given a user who hasn't discovered an item, when calling for said item by its ID, an ItemUndiscoveredException should be thrown`() {
            val userId = createTestUserAndGetId(db)

            assertFailsWith<ItemUndiscoveredException> {
                dexUC.getIndividualDexBaseItem(userId.value, ItemEnum.COPPER_ORE.ordinal)
            }
        }
    }

    @Nested
    inner class DexUnits {
        // TODO: Incomplete tests
    }

    @Nested
    inner class IndividualDexUnit {
        // TODO: Incomplete tests
    }
}
