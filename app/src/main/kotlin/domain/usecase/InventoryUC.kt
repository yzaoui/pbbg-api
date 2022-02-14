package com.bitwiserain.pbbg.app.domain.usecase

import com.bitwiserain.pbbg.app.domain.model.Inventory

interface InventoryUC {
    fun getInventory(userId: Int, filter: InventoryFilter? = null): Inventory
}

enum class InventoryFilter {
    PLANTABLE
}
