package com.bitwiserain.pbbg.domain.model.mine

import com.bitwiserain.pbbg.LevelUp
import com.bitwiserain.pbbg.domain.model.Item

class MineActionResult(
    val minedItemResults: List<MinedItemResult>,
    val levelUps: List<LevelUp>
)

class MinedItemResult(
    val item: Item,
    val expPerIndividualItem: Int
)
