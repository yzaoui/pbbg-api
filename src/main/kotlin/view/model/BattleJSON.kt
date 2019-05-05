package com.bitwiserain.pbbg.view.model

import com.google.gson.annotations.SerializedName

class BattleJSON(
    @SerializedName("allies") val allies: List<MyUnitJSON>,
    @SerializedName("enemies") val enemies: List<MyUnitJSON>,
    @SerializedName("turns") val turns: List<TurnJSON>
)

class TurnJSON(
    @SerializedName("unitId") val unitId: Long
)
