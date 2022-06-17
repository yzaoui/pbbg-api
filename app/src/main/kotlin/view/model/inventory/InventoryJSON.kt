package com.bitwiserain.pbbg.app.view.model.inventory

import com.bitwiserain.pbbg.app.view.model.EquipmentJSON
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InventoryJSON(
    @SerialName("items") val items: List<InventoryItemJSON>,
    @SerialName("equipment") val equipment: EquipmentJSON
)
