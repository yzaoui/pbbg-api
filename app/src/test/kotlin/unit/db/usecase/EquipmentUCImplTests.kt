package com.bitwiserain.pbbg.app.test.unit.db.usecase

import com.bitwiserain.pbbg.app.db.repository.InventoryTable
import com.bitwiserain.pbbg.app.db.usecase.EquipmentUCImpl
import com.bitwiserain.pbbg.app.domain.model.InventoryItem
import com.bitwiserain.pbbg.app.domain.model.InventoryItem.EquippableInventoryItem
import com.bitwiserain.pbbg.app.domain.model.MaterializedItem.*
import com.bitwiserain.pbbg.app.domain.usecase.InventoryItemAlreadyEquippedException
import com.bitwiserain.pbbg.app.domain.usecase.InventoryItemNotEquippableException
import com.bitwiserain.pbbg.app.domain.usecase.InventoryItemNotEquippedException
import com.bitwiserain.pbbg.app.domain.usecase.InventoryItemNotFoundException
import com.bitwiserain.pbbg.app.test.db.TestTransaction
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class EquipmentUCImplTests {

    private val userId: Int = 1234

    private val inventoryTable: InventoryTable = mockk(relaxUnitFun = true)

    private val equipmentUC: EquipmentUCImpl = EquipmentUCImpl(TestTransaction, inventoryTable)

    @Nested
    inner class Equip {
        @Test
        fun `Given a user with an equippable item in inventory that is not equipped, when equipping it, it should be updated as equipped`() {
            val itemId = 0L
            every { inventoryTable.getInventoryItems(userId) } returns mapOf(
                itemId to EquippableInventoryItem(SquarePickaxe, equipped = false)
            )

            equipmentUC.equip(userId, itemId)

            verifySequence {
                inventoryTable.getInventoryItems(userId)
                inventoryTable.setItemEquipped(userId, itemId, equipped = true)
            }
        }

        @Test
        fun `Given a user with a pickaxe already equipped, when equipping another one, it should replace the currently equipped one`() {
            val originallyEquippedItemId = 0L
            val originallyUnequippedItemId = 1L
            every { inventoryTable.getInventoryItems(userId) } returns mapOf(
                originallyEquippedItemId to EquippableInventoryItem(SquarePickaxe, equipped = true),
                originallyUnequippedItemId to EquippableInventoryItem(CrossPickaxe, equipped = false)
            )

            /* Equip second pickaxe */
            equipmentUC.equip(userId, originallyUnequippedItemId)

            verifySequence {
                inventoryTable.getInventoryItems(userId)
                inventoryTable.setItemEquipped(userId, originallyEquippedItemId, equipped = false)
                inventoryTable.setItemEquipped(userId, originallyUnequippedItemId, equipped = true)
            }
        }

        @Test
        fun `Given a user, when equipping an item not in inventory, or which doesn't exist, should throw InventoryItemNotFoundException`() {
            every { inventoryTable.getInventoryItems(userId) } returns emptyMap()

            assertThrows<InventoryItemNotFoundException>("Equipping an item not in inventory should throw an InventoryItemNotFoundException") {
                equipmentUC.equip(userId, 123L)
            }
        }

        @Test
        fun `Given a user, when equipping a non-equippable held item, should throw InventoryItemNotEquippableException`() {
            val itemId = 0L
            every { inventoryTable.getInventoryItems(userId) } returns mapOf(
                itemId to InventoryItem(CopperOre(quantity = 5))
            )

            assertThrows<InventoryItemNotEquippableException>("Equipping a non-equippable item should throw InventoryItemNotEquippableException") {
                equipmentUC.equip(userId, itemId)
            }
        }

        @Test
        fun `Given a user, when equipping an already equipped item, should throw InventoryItemAlreadyEquippedException`() {
            val itemId = 0L
            every { inventoryTable.getInventoryItems(userId) } returns mapOf(
                itemId to EquippableInventoryItem(SquarePickaxe, equipped = true)
            )

            assertThrows<InventoryItemAlreadyEquippedException>("Equipping an already-equipped item should throw InventoryItemAlreadyEquippedException") {
                equipmentUC.equip(userId, itemId)
            }
        }
    }

    @Nested
    inner class Unequip {
        @Test
        fun `Given a user with an equipped item, when unequipping it, it should return unequipped`() {
            val itemId = 0L
            every { inventoryTable.getInventoryItem(userId, itemId) } returns EquippableInventoryItem(IcePick, equipped = true)

            equipmentUC.unequip(userId, itemId)

            verifySequence {
                inventoryTable.getInventoryItem(userId, itemId)
                inventoryTable.setItemEquipped(userId, itemId, equipped = false)
            }
        }

        @Test
        fun `Given a user, when unequipping an item not in inventory, or which doesn't exist, should throw InventoryItemNotFoundException`() {
            val itemId = 0L
            every { inventoryTable.getInventoryItem(userId, itemId) } returns null

            assertThrows<InventoryItemNotFoundException>("Unequipping an item not in inventory should throw an InventoryItemNotFoundException") {
                equipmentUC.unequip(userId, itemId)
            }
        }

        @Test
        fun `Given a user, when unequipping a non-equippable held item, should throw InventoryItemNotEquippableException`() {
            val itemId = 0L
            val item = InventoryItem(Stone(quantity = 1))

            assert(item !is InventoryItem.Equippable)

            every { inventoryTable.getInventoryItem(userId, itemId) } returns item

            assertThrows<InventoryItemNotEquippableException>("Unequipping a non-equippable item should throw InventoryItemNotEquippableException") {
                equipmentUC.unequip(userId, itemId)
            }
        }

        @Test
        fun `Given a user, when unequipping an already unequipped item, should throw InventoryItemNotEquippedException`() {
            val itemId = 0L
            every { inventoryTable.getInventoryItem(userId, itemId) } returns EquippableInventoryItem(CrossPickaxe, equipped = false)

            assertThrows<InventoryItemNotEquippedException>("Unequipping an already-unequipped item should throw InventoryItemNotEquippedException") {
                equipmentUC.unequip(userId, itemId)
            }
        }
    }
}
