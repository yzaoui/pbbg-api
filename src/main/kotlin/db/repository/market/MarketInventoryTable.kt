package com.bitwiserain.pbbg.db.repository.market

import com.bitwiserain.pbbg.db.repository.MaterializedItemTable
import com.bitwiserain.pbbg.db.repository.MaterializedItemTableImpl
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

        val marketId = reference("market_id", MarketTableImpl.Exposed)
        val materializedItem = reference("materialized_item", MaterializedItemTableImpl.Exposed)

        override val primaryKey = PrimaryKey(marketId, materializedItem)
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
