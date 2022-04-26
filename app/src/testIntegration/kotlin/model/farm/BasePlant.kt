package com.bitwiserain.pbbg.app.testintegration.model.farm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BasePlant(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,
    @SerialName("icon") val icon: String,
    @SerialName("growingPeriod") val growingPeriod: Long,
    @SerialName("growingSprite") val growingSprite: String,
    @SerialName("maturePeriod") val maturePeriod: Long?,
    @SerialName("matureSprite") val matureSprite: String?
)
