package com.bitwiserain.pbbg.app.db.usecase

import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.InventoryTable
import com.bitwiserain.pbbg.app.domain.model.BaseItem
import com.bitwiserain.pbbg.app.domain.model.Inventory
import com.bitwiserain.pbbg.app.domain.model.InventoryItem
import com.bitwiserain.pbbg.app.domain.model.MaterializedItem
import com.bitwiserain.pbbg.app.domain.usecase.InventoryFilter
import com.bitwiserain.pbbg.app.domain.usecase.InventoryUC

class InventoryUCImpl(private val transaction: Transaction, private val inventoryTable: InventoryTable) : InventoryUC {
    override fun getInventory(userId: Int, filter: InventoryFilter?): Inventory = transaction {
        return@transaction inventoryTable.getInventoryItems(userId)
            .filterOutZeroQuantityItems()
            .run { if (filter != null) filter { it.value.base.matchesInventoryFilter(filter) } else this }
            .let { Inventory(it) }
    }
}

fun Map<Long, InventoryItem>.filterOutZeroQuantityItems() = filter {
    it.value.item.let { it !is MaterializedItem.Stackable || it.quantity > 0 }
}

private fun BaseItem.matchesInventoryFilter(filter: InventoryFilter): Boolean = when (filter) {
    InventoryFilter.PLANTABLE -> this is BaseItem.Plantable
}
