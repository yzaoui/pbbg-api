package com.bitwiserain.pbbg.app.view.model.battle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator("type")
sealed class UnitEffectJSON {
    @Serializable
    @SerialName("health")
    data class HealthJSON(
        @SerialName("delta") val delta: Int
    ) : UnitEffectJSON()
}
