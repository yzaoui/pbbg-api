package com.bitwiserain.pbbg.view.model

import com.google.gson.annotations.SerializedName

data class ItemJSON(
    @SerializedName("baseId") val baseId: Int,
    @SerializedName("friendlyName") val friendlyName: String,
    @SerializedName("imgURL") val imgURL: String,
    @SerializedName("quantity") val quantity: Int?,
    @SerializedName("description") val description: String
)
