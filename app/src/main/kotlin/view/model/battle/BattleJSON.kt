package com.bitwiserain.pbbg.app.view.model.battle

import com.bitwiserain.pbbg.app.view.model.MyUnitJSON
import com.google.gson.annotations.SerializedName

data class BattleJSON(
    @SerializedName("allies") val allies: List<MyUnitJSON>,
    @SerializedName("enemies") val enemies: List<MyUnitJSON>,
    @SerializedName("turns") val turns: List<TurnJSON>
)


