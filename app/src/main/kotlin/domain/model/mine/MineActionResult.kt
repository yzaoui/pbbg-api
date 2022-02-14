package com.bitwiserain.pbbg.app.domain.model.mine

import com.bitwiserain.pbbg.app.domain.model.LevelProgress
import com.bitwiserain.pbbg.app.domain.model.LevelUp
import com.bitwiserain.pbbg.app.domain.model.MaterializedItem

class MineActionResult(
    val minedItemResults: List<MinedItemResult>,
    val levelUps: List<LevelUp>,
    val mine: Mine,
    val miningLvl: LevelProgress
)

class MinedItemResult(
    val id: Long,
    val item: MaterializedItem,
    val expPerIndividualItem: Int
)
