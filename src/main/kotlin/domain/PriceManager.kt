package com.bitwiserain.pbbg.domain

import com.bitwiserain.pbbg.domain.model.MaterializedItem
import com.bitwiserain.pbbg.domain.model.MaterializedItem.*

object PriceManager {
    fun getSellPrice(item: MaterializedItem): Int = when (item) {
        is Stone, is Coal, is CopperOre -> 3
        is IcePick -> 5
        is PlusPickaxe, is CrossPickaxe, is SquarePickaxe -> 7
    }

    fun getBuyPrice(item: MaterializedItem): Int = when (item) {
        is Stone, is Coal, is CopperOre -> 10
        is IcePick -> 15
        is PlusPickaxe, is CrossPickaxe, is SquarePickaxe -> 20
    }
}
