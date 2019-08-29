package com.bitwiserain.pbbg.view.model.battle

import com.bitwiserain.pbbg.view.model.MaterializedItemJSON

data class BattleRewardJSON(
    val gold: Int,
    val items: List<MaterializedItemJSON>
)
