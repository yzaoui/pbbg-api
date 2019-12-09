package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.Inventory

interface InventoryUC {
    fun getInventory(userId: Int): Inventory
}
