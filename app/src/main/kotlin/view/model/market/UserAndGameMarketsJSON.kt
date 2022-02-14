package com.bitwiserain.pbbg.view.model.market

data class UserAndGameMarketsJSON(
    val gold: Long,
    val userMarket: MarketJSON,
    val gameMarket: MarketJSON
)
