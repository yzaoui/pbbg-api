package com.bitwiserain.pbbg.view.model.inventory

import com.bitwiserain.pbbg.view.model.MaterializedItemJSON

data class InventoryItemJSON(
    val item: MaterializedItemJSON,
    val equipped: Boolean?
)
