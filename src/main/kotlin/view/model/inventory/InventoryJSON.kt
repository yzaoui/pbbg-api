package com.bitwiserain.pbbg.view.model.inventory

import com.bitwiserain.pbbg.view.model.EquipmentJSON

data class InventoryJSON(
    val items: List<InventoryItemJSON>,
    val equipment: EquipmentJSON
)
