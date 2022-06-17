package com.bitwiserain.pbbg.app.view.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseItemJSON(
    @SerialName("id") val id: Int,
    @SerialName("friendlyName") val friendlyName: String,
    @SerialName("img16") val img16: String,
    @SerialName("img32") val img32: String,
    @SerialName("img64") val img64: String,
    @SerialName("description") val description: String,
    @SerialName("grid") val grid: Set<PointJSON>?
)
