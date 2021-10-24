package com.bitwiserain.pbbg.test.unit.db.usecase

import com.bitwiserain.pbbg.SchemaHelper
import com.bitwiserain.pbbg.db.repository.InventoryTableImpl
import com.bitwiserain.pbbg.db.repository.Joins
import com.bitwiserain.pbbg.db.repository.MaterializedItemTableImpl
import com.bitwiserain.pbbg.db.repository.UserTableImpl
import com.bitwiserain.pbbg.db.usecase.InventoryUCImpl
import com.bitwiserain.pbbg.domain.model.BaseItem
import com.bitwiserain.pbbg.domain.model.Inventory
import com.bitwiserain.pbbg.domain.model.MaterializedItem
import com.bitwiserain.pbbg.domain.usecase.InventoryUC
import com.bitwiserain.pbbg.test.createTestUserAndGetId
import com.bitwiserain.pbbg.test.initDatabase
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class InventoryUCImplTests {

    private val db = initDatabase()
    private val inventoryTable = InventoryTableImpl()
    private val materializedItemTable = MaterializedItemTableImpl()
    private val userTable = UserTableImpl()
    private val inventoryUC: InventoryUC = InventoryUCImpl(db)

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(db)
    }

    @Test
    fun `Given a user with items, when calling for inventory, those items should return`() {
        val userId = createTestUserAndGetId(db, userTable)

        val (expectedInventory, actualInventory) = transaction(db) {
            val itemsById = listOf(MaterializedItem.CrossPickaxe, MaterializedItem.CopperOre(quantity = 5), MaterializedItem.IcePick)
                .associateBy { materializedItemTable.insertItemAndGetId(it) }
            // Insert items
            inventoryTable.insertItems(userId, itemsById.mapValues { it.value.base })
            // Get cross pickaxe ID
            val pickId = itemsById.filterValues { it.base is BaseItem.Pickaxe.CrossPickaxe }.asSequence().single().key
            // Equip cross pickaxe
            InventoryTableImpl.Exposed.update({ InventoryTableImpl.Exposed.userId.eq(userId) and InventoryTableImpl.Exposed.materializedItem.eq(pickId) }) { it[equipped] = true }

            return@transaction Inventory(Joins.getInventoryItems(userId)) to inventoryUC.getInventory(userId)
        }

        // TODO: Better assertions, not testing equality here
        assertEquals(actualInventory.items.size, expectedInventory.items.size)
    }
}
