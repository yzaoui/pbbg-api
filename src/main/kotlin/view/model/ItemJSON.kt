package com.bitwiserain.pbbg.view.model

import com.google.gson.annotations.SerializedName

data class ItemJSON(
    @SerializedName("typeId") val typeId: Int,
    @SerializedName("friendlyName") val friendlyName: String,
    @SerializedName("imgURL") val imgURL: String
)
