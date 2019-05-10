package com.bitwiserain.pbbg.domain.model.battle

import com.bitwiserain.pbbg.domain.model.Item

data class BattleReward(
    val gold: Int,
    val items: List<Item>
)
