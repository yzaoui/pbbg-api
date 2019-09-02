package com.bitwiserain.pbbg.view.model

import com.google.gson.annotations.SerializedName

data class UserStatsJSON(
    @SerializedName("username") val username: String,
    @SerializedName("gold") val gold: Long,
    @SerializedName("mining") val miningLvlProgress: LevelProgressJSON
)
