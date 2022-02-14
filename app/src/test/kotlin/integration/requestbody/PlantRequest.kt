package com.bitwiserain.pbbg.test.integration.requestbody

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlantRequest(
    @SerialName("plotId") val plotId: Long,
    @SerialName("itemId") val itemId: Long
)
