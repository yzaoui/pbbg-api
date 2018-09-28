package com.bitwiserain.pbbg.domain.model.mine

import com.bitwiserain.pbbg.domain.model.Item
import com.bitwiserain.pbbg.domain.model.LevelUp

class MineActionResult(
    val minedItemResults: List<MinedItemResult>,
    val levelUps: List<LevelUp>
)

class MinedItemResult(
    val item: Item,
    val expPerIndividualItem: Int
)
