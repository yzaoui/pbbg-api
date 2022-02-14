package com.bitwiserain.pbbg.app.view.model.inventory

import com.bitwiserain.pbbg.app.view.model.EquipmentJSON

data class InventoryJSON(
    val items: List<InventoryItemJSON>,
    val equipment: EquipmentJSON
)
