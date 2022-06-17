package com.bitwiserain.pbbg.app.view.model.battle

import com.bitwiserain.pbbg.app.view.model.MaterializedItemJSON
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BattleRewardJSON(
    @SerialName("gold") val gold: Int,
    @SerialName("items") val items: List<MaterializedItemJSON>
)
