package com.bitwiserain.pbbg.app.test.integration.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Inventory(
    @SerialName("items") val items: List<InventoryItem>,
    @SerialName("equipment") val equipment: Equipment
)
