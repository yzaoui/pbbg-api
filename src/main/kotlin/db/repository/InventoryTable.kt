package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.BaseItem
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*

object InventoryTable : Table() {
    val userId = reference("user_id", UserTable).primaryKey()
    val materializedItem = reference("materialized_item", MaterializedItemTable).primaryKey()
    val equipped = bool("equipped").nullable()

    fun insertItem(userId: EntityID<Int>, itemId: EntityID<Long>, baseItem: BaseItem) = insert {
        it[InventoryTable.userId] = userId
        it[InventoryTable.materializedItem] = itemId
        if (baseItem is BaseItem.Equippable) it[InventoryTable.equipped] = false
    }

    fun insertItems(userId: EntityID<Int>, itemIds: Map<Long, BaseItem>) = batchInsert(itemIds.asIterable()) { entry ->
        this[InventoryTable.userId] = userId
        this[InventoryTable.materializedItem] = EntityID(entry.key, MaterializedItemTable)
        if (entry.value is BaseItem.Equippable) this[InventoryTable.equipped] = false
    }

    fun removeItems(userId: EntityID<Int>, itemIds: Iterable<Long>) = deleteWhere {
        InventoryTable.userId.eq(userId) and InventoryTable.materializedItem.inList(itemIds)
    }
}
