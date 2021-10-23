package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.BaseItem
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

interface InventoryTable {

    fun insertItem(userId: Int, itemId: Long, baseItem: BaseItem)

    fun insertItems(userId: Int, itemEntries: Map<Long, BaseItem>)

    fun removeItem(userId: Int, itemId: Long)

    fun removeItems(userId: Int, itemIds: Iterable<Long>)
}

class InventoryTableImpl : InventoryTable {

    object Exposed : Table(name = "Inventory") {

        val userId = reference("user_id", UserTable)
        val materializedItem = reference("materialized_item", MaterializedItemTableImpl.Exposed)
        val equipped = bool("equipped").nullable()

        override val primaryKey = PrimaryKey(userId, materializedItem)
    }

    override fun insertItem(userId: Int, itemId: Long, baseItem: BaseItem) {
        Exposed.insert {
            it[Exposed.userId] = EntityID(userId, UserTable)
            it[Exposed.materializedItem] = EntityID(itemId, MaterializedItemTableImpl.Exposed)
            if (baseItem is BaseItem.Equippable) it[Exposed.equipped] = false
        }
    }

    override fun insertItems(userId: Int, itemEntries: Map<Long, BaseItem>) {
        Exposed.batchInsert(itemEntries.asIterable()) { entry ->
            this[Exposed.userId] = EntityID(userId, UserTable)
            this[Exposed.materializedItem] = EntityID(entry.key, MaterializedItemTableImpl.Exposed)
            if (entry.value is BaseItem.Equippable) this[Exposed.equipped] = false
        }
    }

    override fun removeItem(userId: Int, itemId: Long) {
        Exposed.deleteWhere {
            Exposed.userId.eq(userId) and Exposed.materializedItem.eq(itemId)
        }
    }

    override fun removeItems(userId: Int, itemIds: Iterable<Long>) {
        Exposed.deleteWhere {
            Exposed.userId.eq(userId) and Exposed.materializedItem.inList(itemIds)
        }
    }
}
