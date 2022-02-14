package com.bitwiserain.pbbg.app.view.model

import com.google.gson.annotations.SerializedName

data class BaseItemJSON(
    @SerializedName("id") val id: Int,
    @SerializedName("friendlyName") val friendlyName: String,
    @SerializedName("img16") val img16: String,
    @SerializedName("img32") val img32: String,
    @SerializedName("img64") val img64: String,
    @SerializedName("description") val description: String,
    @SerializedName("grid") val grid: Set<PointJSON>?
)
