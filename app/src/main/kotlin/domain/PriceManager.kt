package com.bitwiserain.pbbg.app.domain

import com.bitwiserain.pbbg.app.domain.model.MaterializedItem
import com.bitwiserain.pbbg.app.domain.model.MaterializedItem.*

object PriceManager {
    fun getSellPrice(item: MaterializedItem): Int = when (item) {
        is Stone, is Coal, is CopperOre -> 3
        is IcePick -> 5
        is PlusPickaxe, is CrossPickaxe -> 7
        is SquarePickaxe -> 10
        is AppleSapling -> 8
        is TomatoSeed -> 3
        is Apple -> 3
        is Tomato -> 5
    }

    fun getBuyPrice(item: MaterializedItem): Int = when (item) {
        is Stone, is Coal, is CopperOre -> 10
        is IcePick -> 15
        is PlusPickaxe, is CrossPickaxe -> 20
        is SquarePickaxe -> 25
        is AppleSapling -> 20
        is TomatoSeed -> 10
        is Apple -> 5
        is Tomato -> 7
    }
}
