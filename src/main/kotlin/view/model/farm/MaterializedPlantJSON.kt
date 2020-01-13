package com.bitwiserain.pbbg.view.model.farm

import com.google.gson.annotations.SerializedName

data class MaterializedPlantJSON(
    @SerializedName("basePlant") val basePlant: BasePlantJSON,
    @SerializedName("cycleStart") val cycleStart: String,
    @SerializedName("isMature") val isMature: Boolean?
)
