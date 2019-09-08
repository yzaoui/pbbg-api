package com.bitwiserain.pbbg.db.repository.market

import com.bitwiserain.pbbg.db.repository.MaterializedItemTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert

object MarketInventoryTable: Table() {
    val marketId = reference("market_id", MarketTable).primaryKey()
    val materializedItem = reference("materialized_item", MaterializedItemTable).primaryKey()

    fun insertItem(marketId: EntityID<Int>, itemId: EntityID<Long>) = insert {
        it[MarketInventoryTable.marketId] = marketId
        it[MarketInventoryTable.materializedItem] = itemId
    }
}
