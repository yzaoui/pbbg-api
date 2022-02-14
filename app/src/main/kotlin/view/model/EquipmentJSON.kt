package com.bitwiserain.pbbg.app.view.model

import com.bitwiserain.pbbg.app.view.model.inventory.InventoryItemJSON
import com.google.gson.annotations.SerializedName

data class EquipmentJSON(
    @SerializedName("pickaxe") val pickaxe: InventoryItemJSON?
)
