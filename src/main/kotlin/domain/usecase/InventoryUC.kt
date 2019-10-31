package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.Inventory
import com.bitwiserain.pbbg.domain.model.MaterializedItem

interface InventoryUC {
    fun getInventory(userId: Int): Inventory
}
