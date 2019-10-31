package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.ItemHistoryTable
import com.bitwiserain.pbbg.db.repository.MaterializedItemTable
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemDetails
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistory
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistoryInfo
import com.bitwiserain.pbbg.domain.usecase.ItemNotFoundException
import com.bitwiserain.pbbg.domain.usecase.ItemUC
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class ItemUCImpl(private val db: Database) : ItemUC {
    override fun getItemDetails(itemId: Long): ItemDetails = transaction(db) {
        val item = MaterializedItemTable.getItem(itemId) ?: throw ItemNotFoundException(itemId)

        val itemHistory = ItemHistoryTable.getItemHistoryList(itemId)

        val userInfo = UserTable.getUserById(1)!!.let { mapOf(it.id to it.username) }

        return@transaction ItemDetails(
            item = item,
            history = listOf(
                ItemHistory(Instant.now().minusSeconds(60 * 60 * 1), ItemHistoryInfo.FirstMined(1)),
                ItemHistory(Instant.now().minusSeconds(60 * 60 * 3), ItemHistoryInfo.CreatedWithUser(1)),
                ItemHistory(Instant.now().minusSeconds(60 * 60 * 2), ItemHistoryInfo.CreatedInMarket())
            ),
            linkedUserInfo = userInfo
        )
    }
}
