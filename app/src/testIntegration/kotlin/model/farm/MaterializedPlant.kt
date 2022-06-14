package com.bitwiserain.pbbg.app.testintegration.model.farm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MaterializedPlant(
    @SerialName("basePlant") val basePlant: BasePlant,
    @SerialName("cycleStart") val cycleStart: String,
    @SerialName("isMature") val isMature: Boolean?,
    @SerialName("harvests") val harvests: Int?
)
