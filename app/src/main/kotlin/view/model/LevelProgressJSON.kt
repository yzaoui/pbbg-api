package com.bitwiserain.pbbg.app.view.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LevelProgressJSON(
    @SerialName("level") val level: Int,
    @SerialName("relativeExp") val relativeExp: Long,
    @SerialName("relativeExpToNextLevel") val relativeExpToNextLevel: Long
)
