package com.bitwiserain.pbbg.app.view.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PointJSON(
    @SerialName("x") val x: Int,
    @SerialName("y") val y: Int
)
