package com.bitwiserain.pbbg.app.view.model.battle

import com.google.gson.annotations.SerializedName

sealed class UnitEffectJSON(
    @SerializedName("type") val type: String
) {
    data class HealthJSON(
        @SerializedName("delta") val delta: Int
    ) : UnitEffectJSON("health")
}
