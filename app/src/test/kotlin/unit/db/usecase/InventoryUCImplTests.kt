package com.bitwiserain.pbbg.app.test.unit.db.usecase

import com.bitwiserain.pbbg.app.db.repository.InventoryTable
import com.bitwiserain.pbbg.app.db.usecase.InventoryUCImpl
import com.bitwiserain.pbbg.app.domain.model.InventoryItem
import com.bitwiserain.pbbg.app.domain.model.MaterializedItem
import com.bitwiserain.pbbg.app.test.db.TestTransaction
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class InventoryUCImplTests {

    private val userId: Int = 1234

    private val inventoryTable: InventoryTable = mockk {
        every { getInventoryItems(userId) } returns mapOf()
    }

    private val inventoryUC: InventoryUCImpl = InventoryUCImpl(TestTransaction, inventoryTable)

    @Test
    fun `Given a user with items, when calling for inventory, those items should return`() {
        val expectedInventoryItems = mapOf(
            0L to InventoryItem(MaterializedItem.CopperOre(quantity = 5)),
            1L to InventoryItem.EquippableInventoryItem(MaterializedItem.CrossPickaxe, equipped = true),
            2L to InventoryItem(MaterializedItem.IcePick),
        )

        every { inventoryTable.getInventoryItems(userId) } returns expectedInventoryItems

        val actualInventory = inventoryUC.getInventory(userId)

        // TODO: Better assertions, not testing equality here
        assertEquals(expectedInventoryItems.size, actualInventory.items.size)
    }
}
