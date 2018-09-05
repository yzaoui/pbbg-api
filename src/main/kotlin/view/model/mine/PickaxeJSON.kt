package com.bitwiserain.pbbg.view.model.mine

import com.google.gson.annotations.SerializedName

data class PickaxeJSON(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: String,
    @SerializedName("cells") val cells: List<IntArray>
)
