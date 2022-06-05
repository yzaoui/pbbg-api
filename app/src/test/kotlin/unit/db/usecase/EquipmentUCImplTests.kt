package com.bitwiserain.pbbg.app.test.unit.db.usecase

import com.bitwiserain.pbbg.app.SchemaHelper
import com.bitwiserain.pbbg.app.db.repository.InventoryTableImpl
import com.bitwiserain.pbbg.app.db.repository.Joins
import com.bitwiserain.pbbg.app.db.repository.MaterializedItemTableImpl
import com.bitwiserain.pbbg.app.db.repository.UserTableImpl
import com.bitwiserain.pbbg.app.db.usecase.EquipmentUCImpl
import com.bitwiserain.pbbg.app.domain.model.BaseItem
import com.bitwiserain.pbbg.app.domain.model.InventoryItem
import com.bitwiserain.pbbg.app.domain.model.InventoryItem.EquippableInventoryItem
import com.bitwiserain.pbbg.app.domain.model.MaterializedItem
import com.bitwiserain.pbbg.app.domain.model.MaterializedItem.*
import com.bitwiserain.pbbg.app.domain.usecase.EquipmentUC
import com.bitwiserain.pbbg.app.domain.usecase.InventoryItemAlreadyEquippedException
import com.bitwiserain.pbbg.app.domain.usecase.InventoryItemNotEquippableException
import com.bitwiserain.pbbg.app.domain.usecase.InventoryItemNotEquippedException
import com.bitwiserain.pbbg.app.domain.usecase.InventoryItemNotFoundException
import com.bitwiserain.pbbg.app.test.createTestUserAndGetId
import com.bitwiserain.pbbg.app.test.initDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class EquipmentUCImplTests {

    private val transaction = initDatabase()
    private val inventoryTable = InventoryTableImpl()
    private val materializedItemTable = MaterializedItemTableImpl()
    private val userTable = UserTableImpl()
    private val equipmentUC: EquipmentUC = EquipmentUCImpl(transaction, inventoryTable)

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(transaction)
    }

    @Nested
    inner class Equip {
        @Test
        fun `Given a user with an equippable item in inventory that is not equipped, when equipping it, it should return equipped`() {
            val userId = createTestUserAndGetId(transaction, userTable)

            /* Insert equippable item into inventory */
            val (itemId, oldItemInInventory) = createItem(userId, item = SquarePickaxe, inInventory = true, equipped = false)

            /* Item is initially unequipped */
            assertFalse((oldItemInInventory as EquippableInventoryItem).equipped)

            /* Equip item */
            equipmentUC.equip(userId, itemId)

            /* Item should now be equipped */
            val newItemInInventory = transaction { Joins.getInventoryItem(userId, itemId) }

            assertNotNull(newItemInInventory, "Item should still exist in inventory after equipping it.")
            assertTrue((newItemInInventory as EquippableInventoryItem).equipped, "Item should be equipped after equip() call.")
        }

        @Test
        fun `Given a user with a pickaxe already equipped, when equipping another one, it should replace the currently equipped one`() {
            val userId = createTestUserAndGetId(transaction, userTable)

            /* Equip a pickaxe */
            val (oldOriginallyEquippedPickaxeId, oldOriginallyEquippedPickaxe) = createItem(userId, item = SquarePickaxe, inInventory = true, equipped = true)

            /* First pickaxe is initially equipped */
            assertTrue((oldOriginallyEquippedPickaxe as EquippableInventoryItem).equipped)

            /* Store a second pickaxe in inventory */
            val (oldPickaxeToEquipId, oldPickaxeToEquip) = createItem(userId, item = CrossPickaxe, inInventory = true, equipped = false)

            /* Second pickaxe is initially unequipped */
            assertFalse((oldPickaxeToEquip as EquippableInventoryItem).equipped)

            /* Equip second pickaxe */
            equipmentUC.equip(userId, oldPickaxeToEquipId)

            /* Originally-equipped pickaxe should be in inventory and unequipped */
            val newOriginallyEquippedPickaxe = transaction { Joins.getInventoryItem(userId, oldOriginallyEquippedPickaxeId) }
            assertNotNull(newOriginallyEquippedPickaxe, "Originally-equipped pickaxe should still be in inventory after equipping another pickaxe.")
            assertFalse(
                (newOriginallyEquippedPickaxe as EquippableInventoryItem).equipped,
                "Originally-equipped pickaxe should now be unequipped after equipping another pickaxe."
            )

            /* Newly-equipped pickaxe should be in inventory and equipped */
            val newPickaxeToEquip = transaction { Joins.getInventoryItem(userId, oldPickaxeToEquipId) }
            assertNotNull(newPickaxeToEquip, "Newly-equipped pickaxe should still be in inventory after equipping it while already having another pickaxe equipped.")
            assertTrue(
                (newPickaxeToEquip as EquippableInventoryItem).equipped,
                "Newly-equipped pickaxe should now be equipped after equipping it while already having another pickaxe equipped."
            )
        }

        @Test
        fun `Given a user, when equipping an item not in inventory, or which doesn't exist, should throw InventoryItemNotFoundException`() {
            val userId = createTestUserAndGetId(transaction, userTable)

            /* Create item but not placed in inventory */
            val (itemId) = createItem(userId, item = SquarePickaxe, inInventory = false, equipped = false)

            assertThrows<InventoryItemNotFoundException>("Equipping an item not in inventory should throw an InventoryItemNotFoundException") {
                equipmentUC.equip(userId, itemId)
            }

            assertThrows<InventoryItemNotFoundException>("Equipping an item that does not exist should throw an InventoryItemNotFoundException") {
                equipmentUC.equip(userId, 10)
            }
        }

        @Test
        fun `Given a user, when equipping a non-equippable held item, should throw InventoryItemNotEquippableException`() {
            val userId = createTestUserAndGetId(transaction, userTable)

            /* Insert equippable item into inventory */
            val (nonequippableItemId) = createItem(userId, item = CopperOre(quantity = 1), inInventory = true)

            assertThrows<InventoryItemNotEquippableException>("Equipping a non-equippable item should throw InventoryItemNotEquippableException") {
                equipmentUC.equip(userId, nonequippableItemId)
            }
        }

        @Test
        fun `Given a user, when equipping an already equipped item, should throw InventoryItemAlreadyEquippedException`() {
            val userId = createTestUserAndGetId(transaction, userTable)

            /* Equip item */
            val (equippedItemId) = createItem(userId, item = CrossPickaxe, inInventory = true, equipped = true)

            assertThrows<InventoryItemAlreadyEquippedException>("Equipping an already-equipped item should throw InventoryItemAlreadyEquippedException") {
                equipmentUC.equip(userId, equippedItemId)
            }
        }
    }

    @Nested
    inner class Unequip {
        @Test
        fun `Given a user with an equipped item, when unequipping it, it should return unequipped`() {
            val userId = createTestUserAndGetId(transaction, userTable)

            /* Equip item */
            val (equippedItemId, equippedItem) = createItem(userId, item = IcePick, inInventory = true, equipped = true)

            /* Item is initially equipped */
            assertTrue((equippedItem as EquippableInventoryItem).equipped)

            /* Unequip item */
            equipmentUC.unequip(userId, equippedItemId)

            /* Item should now be unequipped */
            val newlyUnequippedItem = transaction { Joins.getInventoryItem(userId, equippedItemId) }

            assertNotNull(newlyUnequippedItem, "Item should still exist in inventory after unequipping it.")
            assertFalse((newlyUnequippedItem as EquippableInventoryItem).equipped, "Item should be unequipped after unequip() call.")
        }

        @Test
        fun `Given a user, when unequipping an item not in inventory, or which doesn't exist, should throw InventoryItemNotFoundException`() {
            val userId = createTestUserAndGetId(transaction, userTable)

            /* Create item but not placed in inventory */
            val (itemId) = createItem(userId, item = SquarePickaxe, inInventory = false)

            assertThrows<InventoryItemNotFoundException>("Unequipping an item not in inventory should throw an InventoryItemNotFoundException") {
                equipmentUC.unequip(userId, itemId)
            }

            assertThrows<InventoryItemNotFoundException>("Unequipping an item that does not exist should throw an InventoryItemNotFoundException") {
                equipmentUC.equip(userId, 10)
            }
        }

        @Test
        fun `Given a user, when unequipping a non-equippable held item, should throw InventoryItemNotEquippableException`() {
            val userId = createTestUserAndGetId(transaction, userTable)

            /* Insert equippable item into inventory */
            val (nonequippableItemId) = createItem(userId, item = Stone(quantity = 1), inInventory = true)

            assertThrows<InventoryItemNotEquippableException>("Unequipping a non-equippable item should throw InventoryItemNotEquippableException") {
                equipmentUC.unequip(userId, nonequippableItemId)
            }
        }

        @Test
        fun `Given a user, when unequipping an already unequipped item, should throw InventoryItemNotEquippedException`() {
            val userId = createTestUserAndGetId(transaction, userTable)

            /* Hold item unequipped */
            val (equippedItemId) = createItem(userId, item = CrossPickaxe, inInventory = true, equipped = false)

            assertThrows<InventoryItemNotEquippedException>("Unequipping an already-unequipped item should throw InventoryItemNotEquippedException") {
                equipmentUC.unequip(userId, equippedItemId)
            }
        }
    }

    fun createItem(userId: Int, item: MaterializedItem, inInventory: Boolean, equipped: Boolean? = null): Pair<Long, InventoryItem?> {
        val itemId = transaction {
            val id = materializedItemTable.insertItemAndGetId(item)
            if (inInventory) {
                inventoryTable.insertItem(userId, id, item.base)
                if (item.base is BaseItem.Equippable) Joins.setItemEquipped(userId, id, equipped!!)
            }

            return@transaction id
        }
        val invItem = transaction { Joins.getInventoryItem(userId, itemId) }

        return itemId to invItem
    }
}
