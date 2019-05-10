package com.bitwiserain.pbbg.view.model.battle

import com.google.gson.annotations.SerializedName

class BattleActionResultJSON(
    @SerializedName("battle") val battle: BattleJSON,
    val unitEffects: Map<Long, UnitEffectJSON>,
    val reward: BattleRewardJSON? = null
)
