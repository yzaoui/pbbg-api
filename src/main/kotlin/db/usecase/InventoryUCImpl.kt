package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.Joins
import com.bitwiserain.pbbg.domain.model.BaseItem
import com.bitwiserain.pbbg.domain.model.Inventory
import com.bitwiserain.pbbg.domain.model.InventoryItem
import com.bitwiserain.pbbg.domain.model.MaterializedItem
import com.bitwiserain.pbbg.domain.usecase.InventoryFilter
import com.bitwiserain.pbbg.domain.usecase.InventoryUC
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class InventoryUCImpl(private val db: Database) : InventoryUC {
    override fun getInventory(userId: Int, filter: InventoryFilter?): Inventory = transaction(db) {
        return@transaction Joins.getInventoryItems(userId)
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
