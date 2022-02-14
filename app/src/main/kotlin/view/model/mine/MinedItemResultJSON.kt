package com.bitwiserain.pbbg.app.view.model.mine

import com.bitwiserain.pbbg.app.view.model.MaterializedItemJSON
import com.google.gson.annotations.SerializedName

data class MinedItemResultJSON(
    @SerializedName("item") val item: MaterializedItemJSON,
    @SerializedName("expPerIndividualItem") val expPerIndividualItem: Int
)
