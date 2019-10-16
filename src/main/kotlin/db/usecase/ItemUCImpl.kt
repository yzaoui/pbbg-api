package com.bitwiserain.pbbg.db.usecase

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

        // TODO: Replace all the following mocks

        val userInfo = UserTable.getUserById(1)!!.let { ItemHistoryInfo.UserInfo(it.id, it.username) }

        return@transaction ItemDetails(item, listOf(
            ItemHistory(Instant.now().minusSeconds(60 * 60 * 1), ItemHistoryInfo.Mined(userInfo)),
            ItemHistory(Instant.now().minusSeconds(60 * 60 * 2), ItemHistoryInfo.CreatedWithUser(userInfo)),
            ItemHistory(Instant.now().minusSeconds(60 * 60 * 3), ItemHistoryInfo.CreatedInMarket)
        ))
    }
}
