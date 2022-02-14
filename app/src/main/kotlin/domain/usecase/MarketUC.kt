package com.bitwiserain.pbbg.app.domain.usecase

import com.bitwiserain.pbbg.app.domain.model.market.MarketOrder
import com.bitwiserain.pbbg.app.domain.model.market.UserAndGameMarkets

interface MarketUC {
    fun getMarkets(userId: Int): UserAndGameMarkets
    /**
     * @throws NotEnoughGoldException when not enough gold to make this transaction.
     */
    fun buy(userId: Int, orders: List<MarketOrder>): UserAndGameMarkets
    fun sell(userId: Int, orders: List<MarketOrder>): UserAndGameMarkets
}

class NotEnoughGoldException : Exception()
