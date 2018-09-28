package com.bitwiserain.pbbg.view.model.mine

import com.bitwiserain.pbbg.view.model.ItemJSON
import com.google.gson.annotations.SerializedName

data class MinedItemResultJSON(
    @SerializedName("item") val item: ItemJSON,
    @SerializedName("expPerIndividualItem") val expPerIndividualItem: Int
)
