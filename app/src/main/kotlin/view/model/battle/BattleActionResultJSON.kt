package com.bitwiserain.pbbg.view.model.battle

import com.google.gson.annotations.SerializedName

data class BattleActionResultJSON(
    @SerializedName("battle") val battle: BattleJSON,
    @SerializedName("unitEffects") val unitEffects: Map<Long, UnitEffectJSON>,
    @SerializedName("reward") val reward: BattleRewardJSON? = null
)
