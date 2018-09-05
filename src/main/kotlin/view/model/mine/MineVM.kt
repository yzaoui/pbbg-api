package com.bitwiserain.pbbg.view.model.mine

import com.google.gson.annotations.SerializedName

data class MineVM(
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int,
    @SerializedName("cells") val cells: List<List<MineItemVM?>>
)
