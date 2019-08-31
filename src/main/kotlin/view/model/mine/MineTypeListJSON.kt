package com.bitwiserain.pbbg.view.model.mine

import com.google.gson.annotations.SerializedName

data class MineTypeListJSON(
    @SerializedName("types") val types: List<MineTypeJSON>,
    @SerializedName("nextUnlockLevel") val nextUnlockLevel: Int?
) {
    data class MineTypeJSON(
        @SerializedName("id") val id: Int,
        @SerializedName("name") val name: String,
        @SerializedName("minLevel") val minLevel: Int,
        @SerializedName("bgURL") val backgroundURL: String
    )
}
