package com.bitwiserain.pbbg.app.db.repository

import com.bitwiserain.pbbg.app.domain.model.BaseItem
import com.bitwiserain.pbbg.app.domain.model.InventoryItem
import com.bitwiserain.pbbg.app.domain.model.ItemEnum
import com.bitwiserain.pbbg.app.domain.model.MaterializedItem
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

interface InventoryTable {

    fun insertItem(userId: Int, itemId: Long, baseItem: BaseItem)

    fun insertItems(userId: Int, itemEntries: Map<Long, BaseItem>)

    fun removeItem(userId: Int, itemId: Long)

    fun removeItems(userId: Int, itemIds: Iterable<Long>)

    fun getHeldItemsOfBaseKind(userId: Int, itemEnum: ItemEnum): Map<Long, MaterializedItem>

    fun getInventoryItem(userId: Int, itemId: Long): InventoryItem?

    fun getInventoryItems(userId: Int): Map<Long, InventoryItem>

    fun setItemEquipped(userId: Int, itemId: Long, equipped: Boolean)
}

class InventoryTableImpl : InventoryTable {

    object Exposed : Table(name = "Inventory") {

        val userId = reference("user_id", UserTableImpl.Exposed)
        val materializedItem = reference("materialized_item", MaterializedItemTableImpl.Exposed)
        val equipped = bool("equipped").nullable()

        override val primaryKey = PrimaryKey(userId, materializedItem)
    }

    override fun insertItem(userId: Int, itemId: Long, baseItem: BaseItem) {
        Exposed.insert {
            it[Exposed.userId] = EntityID(userId, UserTableImpl.Exposed)
            it[Exposed.materializedItem] = EntityID(itemId, MaterializedItemTableImpl.Exposed)
            if (baseItem is BaseItem.Equippable) it[Exposed.equipped] = false
        }
    }

    override fun insertItems(userId: Int, itemEntries: Map<Long, BaseItem>) {
        Exposed.batchInsert(itemEntries.asIterable()) { entry ->
            this[Exposed.userId] = EntityID(userId, UserTableImpl.Exposed)
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

    override fun getHeldItemsOfBaseKind(userId: Int, itemEnum: ItemEnum): Map<Long, MaterializedItem> =
        (Exposed innerJoin MaterializedItemTableImpl.Exposed)
            .select { Exposed.userId.eq(userId) and MaterializedItemTableImpl.Exposed.itemEnum.eq(itemEnum) }
            .associate { it[MaterializedItemTableImpl.Exposed.id].value to it.toMaterializedItem() }

    override fun getInventoryItem(userId: Int, itemId: Long): InventoryItem? =
        (Exposed innerJoin MaterializedItemTableImpl.Exposed)
            .select { Exposed.userId.eq(userId) and Exposed.materializedItem.eq(itemId) }
            .singleOrNull()
            ?.toInventoryItem()

    override fun getInventoryItems(userId: Int): Map<Long, InventoryItem> =
        (Exposed innerJoin MaterializedItemTableImpl.Exposed)
            .select { Exposed.userId.eq(userId) }
            .associate { it[MaterializedItemTableImpl.Exposed.id].value to it.toInventoryItem() }

    override fun setItemEquipped(userId: Int, itemId: Long, equipped: Boolean) {
        Exposed.update({ Exposed.userId.eq(userId) and Exposed.materializedItem.eq(itemId) }) {
            it[Exposed.equipped] = equipped
        }
    }
}
