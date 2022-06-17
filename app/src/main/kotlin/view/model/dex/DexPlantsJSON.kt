package com.bitwiserain.pbbg.app.view.model.dex

import com.bitwiserain.pbbg.app.view.model.farm.BasePlantJSON
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Client equivalent of [com.bitwiserain.pbbg.app.domain.model.dex.DexPlants].
 */
@Serializable
data class DexPlantsJSON(
    @SerialName("discoveredPlants") val discoveredPlants: Map<Int, BasePlantJSON>,
    @SerialName("lastPlantId") val lastPlantId: Int
)
