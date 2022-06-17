package com.bitwiserain.pbbg.app.view.model.battle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TurnJSON(
    @SerialName("unitId") val unitId: Long
)
