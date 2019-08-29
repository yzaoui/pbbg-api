package com.bitwiserain.pbbg.domain.model.mine

import com.bitwiserain.pbbg.domain.model.LevelUp
import com.bitwiserain.pbbg.domain.model.MaterializedItem

class MineActionResult(
    val minedItemResults: List<MinedItemResult>,
    val levelUps: List<LevelUp>
)

class MinedItemResult(
    val item: MaterializedItem,
    val expPerIndividualItem: Int
)
