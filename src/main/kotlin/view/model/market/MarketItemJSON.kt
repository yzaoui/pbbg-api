package com.bitwiserain.pbbg.view.model.market

import com.bitwiserain.pbbg.view.model.MaterializedItemJSON

data class MarketItemJSON(
    val id: Int,
    val item: MaterializedItemJSON,
    val price: Int
)
