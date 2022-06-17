package com.bitwiserain.pbbg.app.view.model.market

import com.bitwiserain.pbbg.app.view.model.MaterializedItemJSON
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarketItemJSON(
    @SerialName("item") val item: MaterializedItemJSON,
    @SerialName("price") val price: Int
)
