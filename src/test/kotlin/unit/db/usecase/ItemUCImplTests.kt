package com.bitwiserain.pbbg.test.unit.db.usecase

import com.bitwiserain.pbbg.SchemaHelper
import com.bitwiserain.pbbg.db.repository.ItemHistoryTableImpl
import com.bitwiserain.pbbg.db.repository.MaterializedItemTableImpl
import com.bitwiserain.pbbg.db.usecase.ItemUCImpl
import com.bitwiserain.pbbg.domain.model.MaterializedItem
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistory
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistoryInfo
import com.bitwiserain.pbbg.domain.usecase.ItemNotFoundException
import com.bitwiserain.pbbg.domain.usecase.ItemUC
import com.bitwiserain.pbbg.test.createTestUserAndGetId
import com.bitwiserain.pbbg.test.initDatabase
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ItemUCImplTests {

    private val db = initDatabase()
    private val itemHistoryTable = ItemHistoryTableImpl()
    private val materializedItemTable = MaterializedItemTableImpl()
    private val itemUC: ItemUC = ItemUCImpl(db, itemHistoryTable, materializedItemTable)

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(db)
    }

    @Test
    fun `Given an item in existence with a history, when calling for its details, should return all its expected details`() {
        val creationDate = Instant.ofEpochSecond(946684800L)
        val users = listOf("user1", "user2", "user3")
            .associateBy { createTestUserAndGetId(db, it) }

        val itemId = transaction(db) {
            val itemId = materializedItemTable.insertItemAndGetId(MaterializedItem.Stone(quantity = 3))

            listOf(ItemHistory(
                date = creationDate,
                info = ItemHistoryInfo.CreatedInMarket
            ), ItemHistory(
                date = creationDate.plusSeconds(60),
                info = ItemHistoryInfo.FirstMined(2)
            ), ItemHistory(
                date = creationDate.plusSeconds(120),
                info = ItemHistoryInfo.CreatedWithUser(3)
            )).forEach {
                itemHistoryTable.insertItemHistory(itemId, it)
            }

            return@transaction itemId
        }

        val actualItem = itemUC.getItemDetails(itemId)

        val actualMaterializedItem = actualItem.item
        val actualHistory = actualItem.history
        val actualUserInfo = actualItem.linkedUserInfo

        assertTrue(actualMaterializedItem is MaterializedItem.Stone && actualMaterializedItem.quantity == 3)
        assertTrue(actualHistory.size == 3) // TODO: Test actual equality
        assertTrue(actualUserInfo.size == 2 && actualUserInfo[2] == users[2] && actualUserInfo[3] == users[3])
    }

    @Test
    fun `When calling for an item that doesn't exist, ItemNotFoundException should be thrown`() {
        assertThrows<ItemNotFoundException> {
            itemUC.getItemDetails(4)
        }
    }
}
