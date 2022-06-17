package com.bitwiserain.pbbg.app.view.model.market

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarketJSON(
    @SerialName("items") val items: List<MarketItemJSON>
)
