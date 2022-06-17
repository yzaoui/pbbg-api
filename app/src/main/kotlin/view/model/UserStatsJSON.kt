package com.bitwiserain.pbbg.app.view.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserStatsJSON(
    @SerialName("username") val username: String,
    @SerialName("gold") val gold: Long,
    @SerialName("mining") val miningLvlProgress: LevelProgressJSON,
    @SerialName("farming") val farmingLvlProgress: LevelProgressJSON
)
