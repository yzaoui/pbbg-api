package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.BaseItem
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

object InventoryTable : Table() {
    val userId = reference("user_id", UserTable).primaryKey()
    val materializedItem = reference("materialized_item", MaterializedItemTable).primaryKey()
    val equipped = bool("equipped").nullable()

    fun insertItem(userId: EntityID<Int>, itemId: EntityID<Long>, baseItem: BaseItem) = insert {
        it[InventoryTable.userId] = userId
        it[InventoryTable.materializedItem] = itemId
        if (baseItem is BaseItem.Equippable) it[InventoryTable.equipped] = false
    }

    fun removeItems(userId: EntityID<Int>, itemIds: List<Long>) = deleteWhere {
        InventoryTable.userId.eq(userId) and InventoryTable.materializedItem.inList(itemIds)
    }
}
