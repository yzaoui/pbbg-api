package com.bitwiserain.pbbg.app.test.unit.db.usecase

import com.bitwiserain.pbbg.app.db.model.User
import com.bitwiserain.pbbg.app.db.repository.ItemHistoryTable
import com.bitwiserain.pbbg.app.db.repository.MaterializedItemTable
import com.bitwiserain.pbbg.app.db.repository.UserTable
import com.bitwiserain.pbbg.app.db.usecase.ItemUCImpl
import com.bitwiserain.pbbg.app.domain.model.MaterializedItem
import com.bitwiserain.pbbg.app.domain.model.itemdetails.ItemHistory
import com.bitwiserain.pbbg.app.domain.model.itemdetails.ItemHistoryInfo
import com.bitwiserain.pbbg.app.domain.usecase.ItemNotFoundException
import com.bitwiserain.pbbg.app.domain.usecase.ItemUC
import com.bitwiserain.pbbg.app.test.db.TestTransaction
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ItemUCImplTests {

    private val itemHistoryTable: ItemHistoryTable = mockk()
    private val materializedItemTable: MaterializedItemTable = mockk()
    private val userTable: UserTable = mockk()

    private val itemUC: ItemUC = ItemUCImpl(TestTransaction, itemHistoryTable, materializedItemTable, userTable)

    @Test
    fun `Given an item in existence with a history, when calling for its details, should return all its expected details`() {
        val itemId = 0L
        val creationDate = Instant.ofEpochSecond(946684800L)
        val userIds = setOf(2, 3)
        val users = userIds.associateWith<Int, User> {
            mockk {
                every { id } returns it
                every { username } returns "user$id"
            }
        }
        every { materializedItemTable.getItem(itemId) } returns MaterializedItem.Stone(quantity = 3)
        every { itemHistoryTable.getItemHistoryList(itemId) } returns listOf(
            ItemHistory(
                date = creationDate,
                info = ItemHistoryInfo.CreatedInMarket
            ), ItemHistory(
                date = creationDate.plusSeconds(60),
                info = ItemHistoryInfo.FirstMined(2)
            ), ItemHistory(
                date = creationDate.plusSeconds(120),
                info = ItemHistoryInfo.CreatedWithUser(3)
            )
        )
        every { userTable.getUsersById(userIds) } returns users

        val actualItem = itemUC.getItemDetails(itemId)

        val actualMaterializedItem = actualItem.item
        val actualHistory = actualItem.history
        val actualUserInfo = actualItem.linkedUserInfo

        assertTrue(actualMaterializedItem is MaterializedItem.Stone && actualMaterializedItem.quantity == 3)
        assertTrue(actualHistory.size == 3) // TODO: Test actual equality
        assertTrue(actualUserInfo.size == 2 && actualUserInfo[2] == users[2]!!.username && actualUserInfo[3] == users[3]!!.username)
    }

    @Test
    fun `When calling for an item that doesn't exist, ItemNotFoundException should be thrown`() {
        val itemId = 0L
        every { materializedItemTable.getItem(itemId) } returns null

        assertThrows<ItemNotFoundException> {
            itemUC.getItemDetails(itemId)
        }
    }
}
