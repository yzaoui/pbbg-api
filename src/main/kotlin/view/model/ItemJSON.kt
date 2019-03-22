package com.bitwiserain.pbbg.view.model

import com.google.gson.annotations.SerializedName

data class ItemJSON(
    @SerializedName("baseItem") val baseItem: ItemEnumJSON,
    @SerializedName("quantity") val quantity: Int?,
    @SerializedName("equipped") val equipped: Boolean?,
    @SerializedName("grid") val grid: Set<PointJSON>?
)
