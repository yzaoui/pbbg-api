package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistory
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistoryInfo
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistoryInfo.*
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistoryInfoEnum
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistoryInfoEnum.*
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.time.Instant

object ItemHistoryTable : Table() {
    val itemId = reference("item_id", MaterializedItemTable)
    val date = long("date")
    val enum = enumeration("enum", ItemHistoryInfoEnum::class)
    val info = text("info")

    fun insertItemHistory(itemId: Long, itemHistory: ItemHistory) = insert {
        it[ItemHistoryTable.itemId] = EntityID(itemId, MaterializedItemTable)
        it[ItemHistoryTable.date] = itemHistory.date.epochSecond
        it[ItemHistoryTable.enum] = itemHistory.info.enum
        it[ItemHistoryTable.info] = itemHistory.info.toJSONString()
    }

    fun getItemHistoryList(itemId: Long): List<ItemHistory> = select { ItemHistoryTable.itemId.eq(itemId) }
        .map { it.toItemHistory() }

    private fun ResultRow.toItemHistory() = ItemHistory(
        date = Instant.ofEpochSecond(this[ItemHistoryTable.date]),
        info = itemHistoryInfofromJSONString(this[ItemHistoryTable.enum], this[ItemHistoryTable.info])
    )

    @UnstableDefault
    private fun ItemHistoryInfo.toJSONString(): String = when (this) {
        is CreatedInMarket -> Json.stringify(CreatedInMarket.serializer(), this)
        is CreatedWithUser -> Json.stringify(CreatedWithUser.serializer(), this)
        is FirstMined -> Json.stringify(FirstMined.serializer(), this)
    }

    @UnstableDefault
    private fun itemHistoryInfofromJSONString(enum: ItemHistoryInfoEnum, infoJSON: String) = when (enum) {
        CREATED_IN_MARKET -> Json.parse(CreatedInMarket.serializer(), infoJSON)
        CREATED_WITH_USER -> Json.parse(CreatedWithUser.serializer(), infoJSON)
        FIRST_MINED -> Json.parse(FirstMined.serializer(), infoJSON)
    }
}
