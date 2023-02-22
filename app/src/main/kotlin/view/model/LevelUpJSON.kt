package com.bitwiserain.pbbg.app.view.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LevelUpJSON(
    @SerialName("newLevel") val newLevel: Int,
    @SerialName("additionalMessage") val additionalMessage: String?,
)
