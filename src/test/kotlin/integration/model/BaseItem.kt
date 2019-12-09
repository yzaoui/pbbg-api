package com.bitwiserain.pbbg.test.integration.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseItem(
    @SerialName("friendlyName") val friendlyName: String,
    @SerialName("img16") val img16: String,
    @SerialName("img32") val img32: String,
    @SerialName("img64") val img64: String,
    @SerialName("description") val description: String,
    @SerialName("grid") val grid: Set<Point>?
)
