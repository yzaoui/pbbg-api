package com.bitwiserain.pbbg.app.testintegration.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InventoryItem(
    @SerialName("item") val item: MaterializedItem,
    @SerialName("equipped") val equipped: Boolean?
)
