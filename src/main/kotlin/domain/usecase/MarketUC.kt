package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.market.Market
import com.bitwiserain.pbbg.domain.model.market.MarketOrder

interface MarketUC {
    fun getMarket(userId: Int): Market
    fun getUserInventory(userId: Int): Market
    fun sell(userId: Int, orders: List<MarketOrder>): Market
}
