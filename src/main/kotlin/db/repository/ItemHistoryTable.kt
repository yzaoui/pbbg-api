package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistory
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.time.Instant

object ItemHistoryTable : Table() {
    val itemId = reference("item_id", MaterializedItemTable)
    val date = long("date")
    val info = text("info")

    fun insertItemHistory(itemId: Long, itemHistory: ItemHistory) = insert {
        it[ItemHistoryTable.itemId] = EntityID(itemId, MaterializedItemTable)
        it[ItemHistoryTable.date] = itemHistory.date.epochSecond
        it[ItemHistoryTable.info] = Json.encodeToString(itemHistory.info)
    }

    fun getItemHistoryList(itemId: Long): List<ItemHistory> = select { ItemHistoryTable.itemId.eq(itemId) }
        .map { it.toItemHistory() }

    private fun ResultRow.toItemHistory() = ItemHistory(
        date = Instant.ofEpochSecond(this[ItemHistoryTable.date]),
        info = Json.decodeFromString(this[ItemHistoryTable.info])
    )
}
