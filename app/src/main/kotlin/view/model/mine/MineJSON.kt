package com.bitwiserain.pbbg.app.view.model.mine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MineJSON(
    @SerialName("width") val width: Int,
    @SerialName("height") val height: Int,
    @SerialName("cells") val cells: List<List<MineEntityJSON?>>,
    @SerialName("type") val type: MineTypeJSON
)
