package com.bitwiserain.pbbg.app.db.usecase

import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.ItemHistoryTable
import com.bitwiserain.pbbg.app.db.repository.MaterializedItemTable
import com.bitwiserain.pbbg.app.db.repository.UserTable
import com.bitwiserain.pbbg.app.domain.model.itemdetails.ItemDetails
import com.bitwiserain.pbbg.app.domain.model.itemdetails.ItemHistoryInfo
import com.bitwiserain.pbbg.app.domain.usecase.ItemNotFoundException
import com.bitwiserain.pbbg.app.domain.usecase.ItemUC

class ItemUCImpl(
    private val transaction: Transaction,
    private val itemHistoryTable: ItemHistoryTable,
    private val materializedItemTable: MaterializedItemTable,
    private val userTable: UserTable,
) : ItemUC {

    override fun getItemDetails(itemId: Long): ItemDetails = transaction {
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
