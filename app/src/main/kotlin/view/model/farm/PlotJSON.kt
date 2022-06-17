package com.bitwiserain.pbbg.app.view.model.farm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlotJSON(
    @SerialName("id") val id: Long,
    @SerialName("plant") val plant: MaterializedPlantJSON?
)
