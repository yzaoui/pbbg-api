package com.bitwiserain.pbbg.app.test.unit.db.usecase

import com.bitwiserain.pbbg.app.db.repository.DexTable
import com.bitwiserain.pbbg.app.db.usecase.DexUCImpl
import com.bitwiserain.pbbg.app.domain.model.BaseItem
import com.bitwiserain.pbbg.app.domain.model.ItemEnum
import com.bitwiserain.pbbg.app.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.app.domain.model.dex.DexItem
import com.bitwiserain.pbbg.app.domain.usecase.DexUC
import com.bitwiserain.pbbg.app.domain.usecase.InvalidItemException
import com.bitwiserain.pbbg.app.domain.usecase.InvalidUnitException
import com.bitwiserain.pbbg.app.test.db.TestTransaction
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class DexUCImplTests {

    private val userId: Int = 1234

    private val dexTable: DexTable = mockk()

    private val dexUC: DexUC = DexUCImpl(TestTransaction, dexTable)

    @Nested
    inner class DexItems {
        @Test
        fun `Given a user who's discovered some items, when calling for dex items, those exact ones should return`() {
            val discoveredItemEnums = setOf(ItemEnum.COPPER_ORE, ItemEnum.ICE_PICK)
            every { dexTable.getDiscovered(userId) } returns discoveredItemEnums

            val dexItems = dexUC.getDexItems(userId)

            assertEquals(discoveredItemEnums, dexItems.discoveredItems)
        }

        @Test
        fun `When calling for dex items, lastItemId should be 11`() {
            every { dexTable.getDiscovered(userId) } returns mockk()

            val dexItems = dexUC.getDexItems(userId)

            assertEquals(11, dexItems.lastItemId)
        }
    }

    @Nested
    inner class IndividualDexItem {
        @Test
        fun `Given a user who has discovered an item, when calling for said item by its ID, it should be returned in detail`() {
            val discoveredItem = BaseItem.Material.Coal
            every { dexTable.hasEntry(userId, discoveredItem.enum) } returns true

            val dexItem = dexUC.getIndividualDexBaseItem(userId, discoveredItem.id)

            assertTrue(dexItem is DexItem.DiscoveredDexItem)
            assertEquals(dexItem.baseItem, discoveredItem)
        }

        @Test
        fun `Given a user, when calling for an invalid item by ID, an InvalidItemException should be thrown`() {
            assertFailsWith<InvalidItemException> {
                dexUC.getIndividualDexBaseItem(userId, ItemEnum.entries.size + 7)
            }
        }

        @Test
        fun `Given a user who hasn't discovered an item, when calling for said item by its ID, it should be undiscovered`() {
            val item = ItemEnum.entries.random().baseItem

            every { dexTable.hasEntry(userId, item.enum) } returns false

            val dexItem = dexUC.getIndividualDexBaseItem(userId, item.id)

            assertTrue(dexItem is DexItem.UndiscoveredDexItem)
            assertEquals(item.id, dexItem.id)
        }
    }

    @Nested
    inner class DexUnits {
        @Test
        fun `When calling getDexUnits(), all dex units should be returned`() {
            val dexUnits = dexUC.getDexUnits(userId)

            assertEquals(MyUnitEnum.entries.toSet(), dexUnits.discoveredUnits)
        }
    }

    @Nested
    inner class IndividualDexUnit {
        @Test
        fun `Given a dex unit ID, when calling getDexUnit(), that dex unit should be returned`() {
            val dexUnit = dexUC.getDexUnit(userId, 2)

            assertEquals(MyUnitEnum.entries[1], dexUnit)
        }

        @Test
        fun `Given an invalid dex unit ID, when calling getDexUnit(), InvalidUnitException should be thrown`() {
            assertThrows<InvalidUnitException> {
                dexUC.getDexUnit(userId, 5000)
            }
        }
    }
}
