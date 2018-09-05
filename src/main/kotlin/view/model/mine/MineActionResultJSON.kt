package com.bitwiserain.pbbg.view.model.mine

import com.bitwiserain.pbbg.domain.model.mine.MineActionResult
import com.google.gson.annotations.SerializedName

data class MineActionResultJSON(
    @SerializedName("results") val results: List<MineActionResult>
)
