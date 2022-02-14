package com.bitwiserain.pbbg.view.model

import com.google.gson.annotations.SerializedName

data class LevelUpJSON(
    @SerializedName("newLevel") val newLevel: Int,
    @SerializedName("additionalMessage") val additionalMessage: String? = null
)
