package com.bitwiserain.pbbg.app.domain.model.market

import com.bitwiserain.pbbg.app.domain.model.MaterializedItem

data class MarketItem(
    val item: MaterializedItem,
    val price: Int
)
