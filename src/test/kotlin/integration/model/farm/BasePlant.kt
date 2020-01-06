package com.bitwiserain.pbbg.test.integration.model.farm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BasePlant(
    @SerialName("growingPeriod") val growingPeriod: Long,
    @SerialName("growingSprite") val growingSprite: String,
    @SerialName("maturePeriod") val maturePeriod: Long?,
    @SerialName("matureSprite") val matureSprite: String?
)
