package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.ItemEnum
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

object Joins {
    fun getHeldItemsOfBaseKind(userId: EntityID<Int>, itemEnum: ItemEnum) =
        (InventoryTable innerJoin MaterializedItemTable)
            .select { InventoryTable.userId.eq(userId) and MaterializedItemTable.itemEnum.eq(itemEnum) }
            .associate { it[MaterializedItemTable.id] to it.toMaterializedItem() }

    fun getInventoryItems(userId: EntityID<Int>) =
        (InventoryTable innerJoin MaterializedItemTable)
            .select { InventoryTable.userId.eq(userId) }
            .associate { it[MaterializedItemTable.id].value to it.toInventoryItem() }

    fun getEquippedItems(userId: EntityID<Int>) =
        (InventoryTable innerJoin MaterializedItemTable)
            .select { InventoryTable.userId.eq(userId) and InventoryTable.equipped.eq(true) }
            .associate { it[MaterializedItemTable.id].value to it.toInventoryItem() }

    fun getInventoryItem(userId: EntityID<Int>, itemId: Long) =
        (InventoryTable innerJoin MaterializedItemTable)
            .select { InventoryTable.userId.eq(userId) and InventoryTable.materializedItem.eq(itemId) }
            .map { it.toInventoryItem() }
            .singleOrNull()

    fun setItemEquipped(userId: EntityID<Int>, itemId: Long, equipped: Boolean) =
        InventoryTable.update({ InventoryTable.userId.eq(userId) and InventoryTable.materializedItem.eq(itemId) }) {
            it[InventoryTable.equipped] = equipped
        }
}

