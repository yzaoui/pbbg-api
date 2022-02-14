package com.bitwiserain.pbbg.app.db.usecase

import com.bitwiserain.pbbg.app.db.repository.ItemHistoryTable
import com.bitwiserain.pbbg.app.db.repository.MaterializedItemTable
import com.bitwiserain.pbbg.app.db.repository.UserTable
import com.bitwiserain.pbbg.app.domain.model.itemdetails.ItemDetails
import com.bitwiserain.pbbg.app.domain.model.itemdetails.ItemHistoryInfo
import com.bitwiserain.pbbg.app.domain.usecase.ItemNotFoundException
import com.bitwiserain.pbbg.app.domain.usecase.ItemUC
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class ItemUCImpl(
    private val db: Database,
    private val itemHistoryTable: ItemHistoryTable,
    private val materializedItemTable: MaterializedItemTable,
    private val userTable: UserTable,
) : ItemUC {

    override fun getItemDetails(itemId: Long): ItemDetails = transaction(db) {
        val item = materializedItemTable.getItem(itemId) ?: throw ItemNotFoundException(itemId)

        val itemHistory = itemHistoryTable.getItemHistoryList(itemId)

        val userSet = mutableSetOf<Int>()

        for (info in itemHistory.map { it.info }) {
            if (info is ItemHistoryInfo.HasUserId) userSet.add(info.userId)
        }

        val userInfo = userTable.getUsersById(userSet).mapValues { it.value.username }

        return@transaction ItemDetails(
            item = item,
            history = itemHistory,
            linkedUserInfo = userInfo
        )
    }
}
