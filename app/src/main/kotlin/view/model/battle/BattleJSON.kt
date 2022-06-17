package com.bitwiserain.pbbg.app.view.model.battle

import com.bitwiserain.pbbg.app.view.model.MyUnitJSON
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BattleJSON(
    @SerialName("allies") val allies: List<MyUnitJSON>,
    @SerialName("enemies") val enemies: List<MyUnitJSON>,
    @SerialName("turns") val turns: List<TurnJSON>
)
