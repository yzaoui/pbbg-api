package com.bitwiserain.pbbg.app.view.model.battle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BattleActionResultJSON(
    @SerialName("battle") val battle: BattleJSON,
    @SerialName("unitEffects") val unitEffects: Map<Long, UnitEffectJSON>,
    @SerialName("reward") val reward: BattleRewardJSON?
)
