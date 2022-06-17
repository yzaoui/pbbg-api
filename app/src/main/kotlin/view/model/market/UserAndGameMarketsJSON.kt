package com.bitwiserain.pbbg.app.view.model.market

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserAndGameMarketsJSON(
    @SerialName("gold") val gold: Long,
    @SerialName("userMarket") val userMarket: MarketJSON,
    @SerialName("gameMarket") val gameMarket: MarketJSON
)
