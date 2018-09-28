package com.bitwiserain.pbbg.view.model.mine

import com.bitwiserain.pbbg.view.model.ItemJSON
import com.google.gson.annotations.SerializedName

data class MineActionResultJSON(
    @SerializedName("minedItemResults") val minedItemResults: List<MinedItemResultJSON>,
    @SerializedName("levelUps") val levelUps: List<LevelUpJSON>
)

data class MinedItemResultJSON(
    @SerializedName("item") val item: ItemJSON,
    @SerializedName("expPerIndividualItem") val expPerIndividualItem: Int
)

data class LevelUpJSON(
    @SerializedName("newLevel") val newLevel: Int
)
