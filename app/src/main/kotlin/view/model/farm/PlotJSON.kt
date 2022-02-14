package com.bitwiserain.pbbg.app.view.model.farm

import com.google.gson.annotations.SerializedName

data class PlotJSON(
    @SerializedName("id") val id: Long,
    @SerializedName("plant") val plant: MaterializedPlantJSON?
)
