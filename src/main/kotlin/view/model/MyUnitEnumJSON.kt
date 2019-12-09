package com.bitwiserain.pbbg.view.model

import com.google.gson.annotations.SerializedName

data class MyUnitEnumJSON(
    @SerializedName("id") val id: Int,
    @SerializedName("friendlyName") val friendlyName: String,
    @SerializedName("description") val description: String,
    @SerializedName("iconURL") val iconURL: String,
    @SerializedName("fullURL") val fullURL: String
)
