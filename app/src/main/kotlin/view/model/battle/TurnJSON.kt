package com.bitwiserain.pbbg.app.view.model.battle

import com.google.gson.annotations.SerializedName

data class TurnJSON(
    @SerializedName("unitId") val unitId: Long
)
