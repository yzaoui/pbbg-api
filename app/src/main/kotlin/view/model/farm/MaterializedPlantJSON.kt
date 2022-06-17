package com.bitwiserain.pbbg.app.view.model.farm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MaterializedPlantJSON(
    @SerialName("basePlant") val basePlant: BasePlantJSON,
    @SerialName("cycleStart") val cycleStart: String,
    @SerialName("isMature") val isMature: Boolean?,
    @SerialName("harvests") val harvests: Int?
)
