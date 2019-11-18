package com.bitwiserain.pbbg.domain.model.battle

import com.bitwiserain.pbbg.domain.model.MaterializedItem

data class BattleReward(
    val gold: Int,
    val items: Map<Long, MaterializedItem>
)
