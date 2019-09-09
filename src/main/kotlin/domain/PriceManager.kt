package com.bitwiserain.pbbg.domain

import com.bitwiserain.pbbg.domain.model.MaterializedItem

object PriceManager {
    fun getSellPrice(item: MaterializedItem): Int {
        return 3
    }

    fun getBuyPrice(item: MaterializedItem): Int {
        return 7
    }
}
