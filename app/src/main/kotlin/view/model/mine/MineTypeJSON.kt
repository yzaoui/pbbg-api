package com.bitwiserain.pbbg.app.view.model.mine

import com.google.gson.annotations.SerializedName

data class MineTypeJSON(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("minLevel") val minLevel: Int,
    @SerializedName("bgURL") val backgroundURL: String
)
