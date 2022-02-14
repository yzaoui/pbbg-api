package com.bitwiserain.pbbg.app.db.repository

import com.bitwiserain.pbbg.app.domain.model.ItemEnum
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

interface DexTable {

    fun hasEntry(userId: Int, item: ItemEnum): Boolean

    fun getDiscovered(userId: Int): Set<ItemEnum>

    fun insertDiscovered(userId: Int, item: ItemEnum)

    fun insertDiscovered(userId: Int, items: Iterable<ItemEnum>)
}

class DexTableImpl : DexTable {

    object Exposed : Table(name = "Dex") {

        val userId = reference("user_id", UserTableImpl.Exposed)
        val item = enumeration("base_item_ordinal", ItemEnum::class)

        override val primaryKey = PrimaryKey(userId, item)
    }

    override fun hasEntry(userId: Int, item: ItemEnum): Boolean = Exposed
        .select { Exposed.userId.eq(userId) and Exposed.item.eq(item) }
        .singleOrNull() != null

    override fun getDiscovered(userId: Int): Set<ItemEnum> = Exposed
        .select { Exposed.userId.eq(userId) }
        .map { it[Exposed.item] }
        .toSet()

    override fun insertDiscovered(userId: Int, item: ItemEnum) {
        Exposed.insert {
            it[Exposed.userId] = EntityID(userId, UserTableImpl.Exposed)
            it[Exposed.item] = item
        }
    }

    override fun insertDiscovered(userId: Int, items: Iterable<ItemEnum>) {
        Exposed.batchInsert(items) { item ->
            this[Exposed.userId] = EntityID(userId, UserTableImpl.Exposed)
            this[Exposed.item] = item
        }
    }
}
