package com.bitwiserain.pbbg.app.test.db.repository

import com.bitwiserain.pbbg.app.db.repository.market.MarketTable

class MarketTableTestImpl(private val marketIds: MutableMap<Int, Int> = mutableMapOf()) : MarketTable {
    override fun createMarketAndGetId(userId: Int): Int {
        assert(userId !in marketIds)
        val marketId = marketIds.size
        marketIds[userId] = marketId
        return marketId
    }
}
