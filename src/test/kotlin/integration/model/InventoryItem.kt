package com.bitwiserain.pbbg.test.integration.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InventoryItem(
    @SerialName("item") val item: MaterializedItem,
    @SerialName("equipped") val equipped: Boolean?
)
