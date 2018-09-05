package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.Inventory
import com.bitwiserain.pbbg.domain.model.Item

interface InventoryUC {
    fun getInventory(userId: Int): Inventory
    fun storeInInventory(userId: Int, item: Item, quantity: Int)
}
