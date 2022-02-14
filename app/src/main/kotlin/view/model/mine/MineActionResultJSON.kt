package com.bitwiserain.pbbg.view.model.mine

import com.bitwiserain.pbbg.view.model.LevelProgressJSON
import com.bitwiserain.pbbg.view.model.LevelUpJSON
import com.google.gson.annotations.SerializedName

data class MineActionResultJSON(
    @SerializedName("minedItemResults") val minedItemResults: List<MinedItemResultJSON>,
    @SerializedName("levelUps") val levelUps: List<LevelUpJSON>,
    @SerializedName("mine") val mine: MineJSON,
    @SerializedName("miningLvl") val miningLvl: LevelProgressJSON
)
