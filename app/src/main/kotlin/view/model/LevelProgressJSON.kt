package com.bitwiserain.pbbg.app.view.model

import com.google.gson.annotations.SerializedName

data class LevelProgressJSON(
    @SerializedName("level") val level: Int,
    @SerializedName("relativeExp") val relativeExp: Long,
    @SerializedName("relativeExpToNextLevel") val relativeExpToNextLevel: Long
)
