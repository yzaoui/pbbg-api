package com.bitwiserain.pbbg.view.model

import com.google.gson.annotations.SerializedName

data class PointJSON(
    @SerializedName("x") val x: Int,
    @SerializedName("y") val y: Int
)
