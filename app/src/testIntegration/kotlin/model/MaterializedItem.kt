package com.bitwiserain.pbbg.app.testintegration.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MaterializedItem(
    @SerialName("id") val id: Long,
    @SerialName("baseItem") val baseItem: BaseItem,
    @SerialName("quantity") val quantity: Int?
)
