package com.bitwiserain.pbbg.app.view.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MaterializedItemJSON(
    @SerialName("id") val id: Long,
    @SerialName("baseItem") val baseItem: BaseItemJSON,
    @SerialName("quantity") val quantity: Int?
)
