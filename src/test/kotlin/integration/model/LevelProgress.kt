package com.bitwiserain.pbbg.test.integration.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LevelProgress(
    @SerialName("level") val level: Int,
    @SerialName("relativeExp") val relativeExp: Long,
    @SerialName("relativeExpToNextLevel") val relativeExpToNextLevel: Long
)
