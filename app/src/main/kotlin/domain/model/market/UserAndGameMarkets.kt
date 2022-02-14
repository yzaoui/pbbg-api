package com.bitwiserain.pbbg.domain.model.market

data class UserAndGameMarkets(
    val gold: Long,
    val userMarket: Market,
    val gameMarket: Market
)
