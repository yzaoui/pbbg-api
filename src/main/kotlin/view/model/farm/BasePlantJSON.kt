package com.bitwiserain.pbbg.view.model.farm

import com.google.gson.annotations.SerializedName

data class BasePlantJSON(
    @SerializedName("growingPeriod") val growingPeriod: Long,
    @SerializedName("growingSprite") val growingSprite: String,
    @SerializedName("maturePeriod") val maturePeriod: Long?,
    @SerializedName("matureSprite") val matureSprite: String?
)
