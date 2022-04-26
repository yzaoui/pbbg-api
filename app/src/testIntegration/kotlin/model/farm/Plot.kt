package com.bitwiserain.pbbg.app.testintegration.model.farm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Plot(
    @SerialName("id") val id: Long,
    @SerialName("plant") val plant: MaterializedPlant?
)
