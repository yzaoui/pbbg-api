package com.bitwiserain.pbbg.app.testintegration.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserStats(
    @SerialName("username") val username: String,
    @SerialName("gold") val gold: Long,
    @SerialName("mining") val mining: LevelProgress,
    @SerialName("farming") val farming: LevelProgress
)
