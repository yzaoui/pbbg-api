package com.bitwiserain.pbbg.view.model.mine

import com.google.gson.annotations.SerializedName

data class MineTypeListJSON(
    @SerializedName("types") val types: List<MineTypeJSON>,
    @SerializedName("nextUnlockLevel") val nextUnlockLevel: Int?
)
