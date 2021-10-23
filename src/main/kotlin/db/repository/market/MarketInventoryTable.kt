package com.bitwiserain.pbbg.db.repository.market

import com.bitwiserain.pbbg.db.repository.MaterializedItemTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

interface MarketInventoryTable {

    fun insertItem(marketId: Int, itemId: Long)

    fun removeItems(itemIds: Set<Long>)
}

class MarketInventoryTableImpl : MarketInventoryTable {

    object Exposed : Table(name = "MarketInventory") {

        val marketId = reference("market_id", MarketTable)
        val materializedItem = reference("materialized_item", MaterializedItemTable)

        override val primaryKey = PrimaryKey(marketId, materializedItem)
    }

    override fun insertItem(marketId: Int, itemId: Long) {
        Exposed.insert {
            it[Exposed.marketId] = EntityID(marketId, MarketTable)
            it[Exposed.materializedItem] = EntityID(itemId, MaterializedItemTable)
        }
    }

    override fun removeItems(itemIds: Set<Long>) {
        Exposed.deleteWhere {
            Exposed.materializedItem.inList(itemIds)
        }
    }
}
