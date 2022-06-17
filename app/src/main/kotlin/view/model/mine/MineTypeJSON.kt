package com.bitwiserain.pbbg.app.view.model.mine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MineTypeJSON(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("minLevel") val minLevel: Int,
    @SerialName("bgURL") val backgroundURL: String
)
