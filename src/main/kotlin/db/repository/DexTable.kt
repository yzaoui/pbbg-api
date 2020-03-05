package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.ItemEnum
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*

object DexTable : Table() {
    val userId = reference("user_id", UserTable).primaryKey()
    val item = enumeration("base_item_ordinal", ItemEnum::class).primaryKey()

    fun hasEntry(userId: Int, item: ItemEnum): Boolean = select { DexTable.userId.eq(userId) and DexTable.item.eq(item) }.singleOrNull() != null

    fun getDiscovered(userId: Int) = select { DexTable.userId.eq(userId) }
        .map { it[DexTable.item] }
        .toSet()

    fun insertDiscovered(userId: Int, item: ItemEnum) = insert {
        it[DexTable.userId] = EntityID(userId, UserTable)
        it[DexTable.item] = item
    }

    fun insertDiscovered(userId: Int, items: Iterable<ItemEnum>) = batchInsert(items) { item ->
        this[DexTable.userId] = EntityID(userId, UserTable)
        this[DexTable.item] = item
    }
}
