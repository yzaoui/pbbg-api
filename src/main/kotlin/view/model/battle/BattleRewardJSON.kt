package com.bitwiserain.pbbg.view.model.battle

import com.bitwiserain.pbbg.view.model.ItemJSON

data class BattleRewardJSON(
    val gold: Int,
    val items: List<ItemJSON>
)
