package com.bitwiserain.pbbg.app.view.model.dex

import com.bitwiserain.pbbg.app.view.model.farm.BasePlantJSON
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Client equivalent of [com.bitwiserain.pbbg.domain.model.dex.DexPlants].
 */
data class DexPlantsJSON(
    @SerializedName("discoveredPlants") val discoveredPlants: SortedMap<Int, BasePlantJSON>,
    @SerializedName("lastPlantId") val lastPlantId: Int
)
