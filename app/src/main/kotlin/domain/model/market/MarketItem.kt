package com.bitwiserain.pbbg.domain.model.market

import com.bitwiserain.pbbg.domain.model.MaterializedItem

data class MarketItem(
    val item: MaterializedItem,
    val price: Int
)
