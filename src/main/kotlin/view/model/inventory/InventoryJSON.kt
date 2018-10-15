package com.bitwiserain.pbbg.view.model.inventory

import com.bitwiserain.pbbg.view.model.EquipmentJSON
import com.bitwiserain.pbbg.view.model.ItemJSON

class InventoryJSON(
    val items: List<ItemJSON>,
    val equipment: EquipmentJSON
)
