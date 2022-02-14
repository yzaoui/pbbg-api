package com.bitwiserain.pbbg.app.domain.model.battle

import com.bitwiserain.pbbg.app.domain.model.MaterializedItem

data class BattleReward(
    val gold: Int,
    val items: Map<Long, MaterializedItem>
)
