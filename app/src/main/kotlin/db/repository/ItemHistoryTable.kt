package com.bitwiserain.pbbg.app.db.repository

import com.bitwiserain.pbbg.app.domain.model.itemdetails.ItemHistory
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.time.Instant

interface ItemHistoryTable {

    fun insertItemHistory(itemId: Long, itemHistory: ItemHistory)

    fun getItemHistoryList(itemId: Long): List<ItemHistory>
}

class ItemHistoryTableImpl : ItemHistoryTable {

    object Exposed : Table(name = "ItemHistory") {
        val itemId = reference("item_id", MaterializedItemTableImpl.Exposed)
        val date = long("date")
        val info = text("info")
    }

    override fun insertItemHistory(itemId: Long, itemHistory: ItemHistory) {
        Exposed.insert {
            it[Exposed.itemId] = EntityID(itemId, MaterializedItemTableImpl.Exposed)
            it[Exposed.date] = itemHistory.date.epochSecond
            it[Exposed.info] = Json.encodeToString(itemHistory.info)
        }
    }

    override fun getItemHistoryList(itemId: Long): List<ItemHistory> = Exposed
        .select { Exposed.itemId.eq(itemId) }
        .map { it.toItemHistory() }

    private fun ResultRow.toItemHistory() = ItemHistory(
        date = Instant.ofEpochSecond(this[Exposed.date]),
        info = Json.decodeFromString(this[Exposed.info])
    )
}
