package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.ItemHistoryTable
import com.bitwiserain.pbbg.db.repository.MaterializedItemTable
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemDetails
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistoryInfo
import com.bitwiserain.pbbg.domain.usecase.ItemNotFoundException
import com.bitwiserain.pbbg.domain.usecase.ItemUC
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class ItemUCImpl(private val db: Database) : ItemUC {
    override fun getItemDetails(itemId: Long): ItemDetails = transaction(db) {
        val item = MaterializedItemTable.getItem(itemId) ?: throw ItemNotFoundException(itemId)

        val itemHistory = ItemHistoryTable.getItemHistoryList(itemId)

        val userSet = mutableSetOf<Int>()

        for (info in itemHistory.map { it.info }) {
            if (info is ItemHistoryInfo.HasUserId) userSet.add(info.userId)
        }

        val userInfo = UserTable.getUsersById(userSet).mapValues { it.value.username }

        return@transaction ItemDetails(
            item = item,
            history = itemHistory,
            linkedUserInfo = userInfo
        )
    }
}
