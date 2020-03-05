package com.bitwiserain.pbbg.db.repository.market

import com.bitwiserain.pbbg.db.repository.MaterializedItemTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert

object MarketInventoryTable: Table() {
    val marketId = reference("market_id", MarketTable)
    val materializedItem = reference("materialized_item", MaterializedItemTable)

    override val primaryKey = PrimaryKey(marketId, materializedItem)

    fun insertItem(marketId: Int, itemId: Long) = insert {
        it[MarketInventoryTable.marketId] = EntityID(marketId, MarketTable)
        it[MarketInventoryTable.materializedItem] = EntityID(itemId, MaterializedItemTable)
    }

    fun removeItems(itemIds: Set<Long>) = deleteWhere {
        MarketInventoryTable.materializedItem.inList(itemIds)
    }
}
