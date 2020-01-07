package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.BaseItem
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*

object InventoryTable : Table() {
    val userId = reference("user_id", UserTable).primaryKey()
    val materializedItem = reference("materialized_item", MaterializedItemTable).primaryKey()
    val equipped = bool("equipped").nullable()

    fun insertItem(userId: Int, itemId: Long, baseItem: BaseItem) = insert {
        it[InventoryTable.userId] = EntityID(userId, UserTable)
        it[InventoryTable.materializedItem] = EntityID(itemId, MaterializedItemTable)
        if (baseItem is BaseItem.Equippable) it[InventoryTable.equipped] = false
    }

    fun insertItems(userId: Int, itemEntries: Map<Long, BaseItem>) = batchInsert(itemEntries.asIterable()) { entry ->
        this[InventoryTable.userId] = EntityID(userId, UserTable)
        this[InventoryTable.materializedItem] = EntityID(entry.key, MaterializedItemTable)
        if (entry.value is BaseItem.Equippable) this[InventoryTable.equipped] = false
    }

    fun removeItem(userId: Int, itemId: Long) = deleteWhere {
        InventoryTable.userId.eq(userId) and InventoryTable.materializedItem.eq(itemId)
    }

    fun removeItems(userId: Int, itemIds: Iterable<Long>) = deleteWhere {
        InventoryTable.userId.eq(userId) and InventoryTable.materializedItem.inList(itemIds)
    }
}
