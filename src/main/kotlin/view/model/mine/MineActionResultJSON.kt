package com.bitwiserain.pbbg.view.model.mine

import com.bitwiserain.pbbg.view.model.LevelUpJSON
import com.google.gson.annotations.SerializedName

data class MineActionResultJSON(
    @SerializedName("minedItemResults") val minedItemResults: List<MinedItemResultJSON>,
    @SerializedName("levelUps") val levelUps: List<LevelUpJSON>
)
