package com.bitwiserain.pbbg.app.view.model.mine

import com.google.gson.annotations.SerializedName

data class MineJSON(
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int,
    @SerializedName("cells") val cells: List<List<MineEntityJSON?>>,
    @SerializedName("type") val type: MineTypeJSON
)
