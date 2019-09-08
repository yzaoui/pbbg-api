package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.market.MarketOrder
import com.bitwiserain.pbbg.domain.model.market.UserAndGameMarkets

interface MarketUC {
    fun getMarkets(userId: Int): UserAndGameMarkets
    fun buy(userId: Int, orders: List<MarketOrder>): UserAndGameMarkets
    fun sell(userId: Int, orders: List<MarketOrder>): UserAndGameMarkets
}
