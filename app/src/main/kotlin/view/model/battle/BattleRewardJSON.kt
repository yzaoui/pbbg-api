package com.bitwiserain.pbbg.app.view.model.battle

import com.bitwiserain.pbbg.app.view.model.MaterializedItemJSON
import com.google.gson.annotations.SerializedName

data class BattleRewardJSON(
    @SerializedName("gold") val gold: Int,
    @SerializedName("items") val items: List<MaterializedItemJSON>
)
