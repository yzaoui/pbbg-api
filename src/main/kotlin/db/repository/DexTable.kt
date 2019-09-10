package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.ItemEnum
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*

object DexTable : Table() {
    val userId = reference("user_id", UserTable).primaryKey()
    val item = enumeration("base_item_ordinal", ItemEnum::class).primaryKey()

    fun hasEntry(userId: EntityID<Int>, item: ItemEnum): Boolean = select { DexTable.userId.eq(userId) and DexTable.item.eq(item) }.singleOrNull() != null

    fun getDiscovered(userId: EntityID<Int>) = select { DexTable.userId.eq(userId) }
        .map { it[DexTable.item] }
        .toSet()

    fun insertDiscovered(userId: EntityID<Int>, item: ItemEnum) = insert {
        it[DexTable.userId] = userId
        it[DexTable.item] = item
    }

    fun insertDiscovered(userId: EntityID<Int>, items: Iterable<ItemEnum>) = batchInsert(items) { item ->
        this[DexTable.userId] = userId
        this[DexTable.item] = item
    }
}
