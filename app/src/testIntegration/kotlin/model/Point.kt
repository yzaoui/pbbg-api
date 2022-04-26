package com.bitwiserain.pbbg.app.testintegration.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Point(
    @SerialName("x") val x: Int,
    @SerialName("y") val y: Int
)
