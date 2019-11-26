package com.bitwiserain.pbbg.test.db.usecase

import com.bitwiserain.pbbg.db.repository.InventoryTable
import com.bitwiserain.pbbg.db.repository.Joins
import com.bitwiserain.pbbg.db.repository.MaterializedItemTable
import com.bitwiserain.pbbg.db.usecase.EquipmentUCImpl
import com.bitwiserain.pbbg.domain.model.InventoryItem
import com.bitwiserain.pbbg.domain.model.MaterializedItem
import com.bitwiserain.pbbg.domain.usecase.EquipmentUC
import com.bitwiserain.pbbg.domain.usecase.InventoryItemAlreadyEquipped
import com.bitwiserain.pbbg.domain.usecase.InventoryItemNotEquippable
import com.bitwiserain.pbbg.domain.usecase.InventoryItemNotFoundException
import com.bitwiserain.pbbg.test.createTestUserAndGetId
import com.bitwiserain.pbbg.test.dropDatabase
import com.bitwiserain.pbbg.test.initDatabase
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class EquipmentUCImplTests {
    private val db = initDatabase()
    private val equipmentUC: EquipmentUC = EquipmentUCImpl(db)

    @AfterEach
    fun dropDatabase() {
        dropDatabase(db)
    }

    @Nested
    inner class Equip {
        @Test
        fun `Given a user with an equippable item in inventory that is not equipped, when equipping it, it should return equipped`() {
            val userId = createTestUserAndGetId(db)

            /* Insert equippable item into inventory */
            val itemId = transaction(db) {
                val item = MaterializedItem.SquarePickaxe
                val id = MaterializedItemTable.insertItemAndGetId(item)
                InventoryTable.insertItem(userId, id, item.base)
                return@transaction id.value
            }

            val oldItemInInventory = transaction(db) { Joins.getInventoryItem(userId, itemId) }

            /* Item is initially unequipped */
            assertFalse((oldItemInInventory as InventoryItem.EquippableInventoryItem).equipped)

            /* Equip item */
            equipmentUC.equip(userId.value, itemId)

            /* Item should now be equipped */
            val newItemInInventory = transaction(db) { Joins.getInventoryItem(userId, itemId) }

            assertNotNull(newItemInInventory, "Item should still exist in inventory after equipping it.")
            assertTrue((newItemInInventory as InventoryItem.EquippableInventoryItem).equipped, "Item should be equipped after equip() call.")
        }

        @Test
        fun `Given a user with a pickaxe already equipped, when equipping another one, it should replace the currently equipped one`() {
            val userId = createTestUserAndGetId(db)

            /* Equip a pickaxe */
            val oldOriginallyEquippedPickaxeId = transaction(db) {
                val pickaxe = MaterializedItem.SquarePickaxe
                val id = MaterializedItemTable.insertItemAndGetId(pickaxe)
                InventoryTable.insertItem(userId, id, pickaxe.base)
                Joins.setItemEquipped(userId, id.value, true)
                return@transaction id.value
            }
            val oldOriginallyEquippedPickaxe = transaction(db) { Joins.getInventoryItem(userId, oldOriginallyEquippedPickaxeId) }
            /* First pickaxe is initially equipped */
            assertTrue((oldOriginallyEquippedPickaxe as InventoryItem.EquippableInventoryItem).equipped)

            /* Store a second pickaxe in inventory */
            val oldPickaxeToEquipId = transaction(db) {
                val pickaxe = MaterializedItem.CrossPickaxe
                val id = MaterializedItemTable.insertItemAndGetId(pickaxe)
                InventoryTable.insertItem(userId, id, pickaxe.base)
                return@transaction id.value
            }
            val oldPickaxeToEquip = transaction(db) { Joins.getInventoryItem(userId, oldPickaxeToEquipId) }
            /* Second pickaxe is initially unequipped */
            assertFalse((oldPickaxeToEquip as InventoryItem.EquippableInventoryItem).equipped)

            /* Equip second pickaxe */
            equipmentUC.equip(userId.value, oldPickaxeToEquipId)

            /* Originally-equipped pickaxe should be in inventory and unequipped */
            val newOriginallyEquippedPickaxe = transaction(db) { Joins.getInventoryItem(userId, oldOriginallyEquippedPickaxeId) }
            assertNotNull(newOriginallyEquippedPickaxe, "Originally-equipped pickaxe should still be in inventory after equipping another pickaxe.")
            assertFalse(
                (newOriginallyEquippedPickaxe as InventoryItem.EquippableInventoryItem).equipped,
                "Originally-equipped pickaxe should now be unequipped after equipping another pickaxe."
            )

            /* Newly-equipped pickaxe should be in inventory and equipped */
            val newPickaxeToEquip = transaction(db) { Joins.getInventoryItem(userId, oldPickaxeToEquipId) }
            assertNotNull(newPickaxeToEquip, "Newly-equipped pickaxe should still be in inventory after equipping it while already having another pickaxe equipped.")
            assertTrue(
                (newPickaxeToEquip as InventoryItem.EquippableInventoryItem).equipped,
                "Newly-equipped pickaxe should now be equipped after equipping it while already having another pickaxe equipped."
            )
        }

        @Test
        fun `Given a user, when equipping an item not in inventory, or which doesn't exist, should throw InventoryItemNotFoundException`() {
            val userId = createTestUserAndGetId(db)

            /* Create item but not placed in inventory */
            val itemId = transaction(db) {
                val id = MaterializedItemTable.insertItemAndGetId(MaterializedItem.SquarePickaxe)
                return@transaction id.value
            }

            assertThrows<InventoryItemNotFoundException>("Equipping an item not in inventory should throw an InventoryItemNotFoundException") {
                equipmentUC.equip(userId.value, itemId)
            }

            assertThrows<InventoryItemNotFoundException>("Equipping an item that does not exist should throw an InventoryItemNotFoundException") {
                equipmentUC.equip(userId.value, 10)
            }
        }

        @Test
        fun `Given a user, when equipping a non-equippable held item, should throw InventoryItemNotEquippableException`() {
            val userId = createTestUserAndGetId(db)

            /* Insert equippable item into inventory */
            val nonequippableItemId = transaction(db) {
                val item = MaterializedItem.CopperOre(quantity = 1)
                val id = MaterializedItemTable.insertItemAndGetId(item)
                InventoryTable.insertItem(userId, id, item.base)
                return@transaction id.value
            }

            assertThrows<InventoryItemNotEquippable>("Equipping a non-equippable item should throw InventoryItemNotEquippableException") {
                equipmentUC.equip(userId.value, nonequippableItemId)
            }
        }

        @Test
        fun `Given a user, when equipping an already equipped item, should throw InventoryItemAlreadyEquippedException`() {
            val userId = createTestUserAndGetId(db)

            /* Equip item */
            val equippedItemId = transaction(db) {
                val pickaxe = MaterializedItem.CrossPickaxe
                val id = MaterializedItemTable.insertItemAndGetId(pickaxe)
                InventoryTable.insertItem(userId, id, pickaxe.base)
                Joins.setItemEquipped(userId, id.value, true)
                return@transaction id.value
            }

            assertThrows<InventoryItemAlreadyEquipped>("Equipping an already-equipped item should throw InventoryItemAlreadyEquippedException") {
                equipmentUC.equip(userId.value, equippedItemId)
            }
        }
    }

    @Nested
    inner class Unequip {
        @Test
        fun `Given a user with an equipped item, when unequipping it, it should return unequipped`() {
            val userId = createTestUserAndGetId(db)

            /* Equip item */
            val equippedItemId = transaction(db) {
                val pickaxe = MaterializedItem.IcePick
                val id = MaterializedItemTable.insertItemAndGetId(pickaxe)
                InventoryTable.insertItem(userId, id, pickaxe.base)
                Joins.setItemEquipped(userId, id.value, true)
                return@transaction id.value
            }
            val equippedItem = transaction(db) { Joins.getInventoryItem(userId, equippedItemId) }

            /* Item is initially equipped */
            assertTrue((equippedItem as InventoryItem.EquippableInventoryItem).equipped)

            /* Unequip item */
            equipmentUC.unequip(userId.value, equippedItemId)

            /* Item should now be unequipped */
            val newlyUnequippedItem = transaction(db) { Joins.getInventoryItem(userId, equippedItemId) }

            assertNotNull(newlyUnequippedItem, "Item should still exist in inventory after unequipping it.")
            assertFalse((newlyUnequippedItem as InventoryItem.EquippableInventoryItem).equipped, "Item should be unequipped after unequip() call.")
        }

        @Test
        fun `Given a user, when unequipping an item not in inventory, or which doesn't exist, should throw InventoryItemNotFoundException`() {
            val userId = createTestUserAndGetId(db)

            /* Create item but not placed in inventory */
            val itemId = transaction(db) {
                val id = MaterializedItemTable.insertItemAndGetId(MaterializedItem.SquarePickaxe)
                return@transaction id.value
            }

            assertThrows<InventoryItemNotFoundException>("Unequipping an item not in inventory should throw an InventoryItemNotFoundException") {
                equipmentUC.unequip(userId.value, itemId)
            }

            assertThrows<InventoryItemNotFoundException>("Unequipping an item that does not exist should throw an InventoryItemNotFoundException") {
                equipmentUC.equip(userId.value, 10)
            }
        }

        @Test
        fun `Given a user, when unequipping a non-equippable held item, should throw InventoryItemNotEquippableException`() {
            val userId = createTestUserAndGetId(db)

            /* Insert equippable item into inventory */
            val nonequippableItemId = transaction(db) {
                val item = MaterializedItem.Stone(quantity = 1)
                val id = MaterializedItemTable.insertItemAndGetId(item)
                InventoryTable.insertItem(userId, id, item.base)
                return@transaction id.value
            }

            assertThrows<InventoryItemNotEquippable>("Unequipping a non-equippable item should throw InventoryItemNotEquippableException") {
                equipmentUC.unequip(userId.value, nonequippableItemId)
            }
        }
    }
}
