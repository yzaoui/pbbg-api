package com.bitwiserain.pbbg.view.model.farm

data class MaterializedPlantJSON(
    val basePlant: BasePlantJSON,
    val cycleStart: String,
    val isMature: Boolean?
)
