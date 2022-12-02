package com.bitwiserain.pbbg.app.db.repository.market

import com.bitwiserain.pbbg.app.db.repository.MaterializedItemTableImpl
import com.bitwiserain.pbbg.app.db.repository.toMaterializedItem
import com.bitwiserain.pbbg.app.domain.model.MaterializedItem
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

interface MarketInventoryTable {

    fun getItems(userId: Int): Map<Long, MaterializedItem>

    fun insertItems(userId: Int, itemIds: Iterable<Long>)

    fun insertItem(marketId: Int, itemId: Long)

    fun removeItems(itemIds: Set<Long>)
}

class MarketInventoryTableImpl : MarketInventoryTable {

    object Exposed : Table(name = "MarketInventory") {

        val marketId = reference("market_id", MarketTableImpl.Exposed)
        val materializedItem = reference("materialized_item", MaterializedItemTableImpl.Exposed)

        override val primaryKey = PrimaryKey(marketId, materializedItem)
    }

    override fun getItems(userId: Int) =
        (Exposed innerJoin MaterializedItemTableImpl.Exposed innerJoin MarketTableImpl.Exposed)
            .select { MarketTableImpl.Exposed.userId.eq(userId) }
            .associate { it[MaterializedItemTableImpl.Exposed.id].value to it.toMaterializedItem() }

    override fun insertItems(userId: Int, itemIds: Iterable<Long>) {
        val marketId = MarketTableImpl.Exposed.select { MarketTableImpl.Exposed.userId.eq(userId) }
            .map { it[MarketTableImpl.Exposed.id] }
            .single()
            .value

        Exposed.batchInsert(itemIds) { itemid ->
            this[Exposed.marketId] = EntityID(marketId, MarketTableImpl.Exposed)
            this[Exposed.materializedItem] = EntityID(itemid, MaterializedItemTableImpl.Exposed)
        }
    }

    override fun insertItem(marketId: Int, itemId: Long) {
        Exposed.insert {
            it[Exposed.marketId] = EntityID(marketId, MarketTableImpl.Exposed)
            it[Exposed.materializedItem] = EntityID(itemId, MaterializedItemTableImpl.Exposed)
        }
    }

    override fun removeItems(itemIds: Set<Long>) {
        Exposed.deleteWhere {
            Exposed.materializedItem.inList(itemIds)
        }
    }
}
