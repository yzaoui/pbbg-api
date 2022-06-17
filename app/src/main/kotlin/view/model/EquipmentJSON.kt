package com.bitwiserain.pbbg.app.view.model

import com.bitwiserain.pbbg.app.view.model.inventory.InventoryItemJSON
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EquipmentJSON(
    @SerialName("pickaxe") val pickaxe: InventoryItemJSON?
)
