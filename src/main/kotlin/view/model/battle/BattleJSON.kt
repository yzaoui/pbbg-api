package com.bitwiserain.pbbg.view.model.battle

import com.bitwiserain.pbbg.view.model.MyUnitJSON
import com.google.gson.annotations.SerializedName

class BattleJSON(
    @SerializedName("allies") val allies: List<MyUnitJSON>,
    @SerializedName("enemies") val enemies: List<MyUnitJSON>,
    @SerializedName("turns") val turns: List<TurnJSON>
)


