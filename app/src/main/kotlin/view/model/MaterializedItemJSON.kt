package com.bitwiserain.pbbg.view.model

import com.google.gson.annotations.SerializedName

data class MaterializedItemJSON(
    @SerializedName("id") val id: Long,
    @SerializedName("baseItem") val baseItem: BaseItemJSON,
    @SerializedName("quantity") val quantity: Int?
)
