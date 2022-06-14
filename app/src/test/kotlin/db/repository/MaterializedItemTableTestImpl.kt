package com.bitwiserain.pbbg.app.test.db.repository

import com.bitwiserain.pbbg.app.db.repository.MaterializedItemTable
import com.bitwiserain.pbbg.app.domain.model.MaterializedItem

class MaterializedItemTableTestImpl(private val items: MutableMap<Long, MaterializedItem> = mutableMapOf()) : MaterializedItemTable {

    override fun getItem(itemId: Long): MaterializedItem? = items[itemId]

    override fun insertItemAndGetId(itemToStore: MaterializedItem): Long {
        val itemId = items.size.toLong()
        items[itemId] = itemToStore
        return itemId
    }

    override fun updateQuantity(itemId: Long, quantityDelta: Int) {
        items.computeIfPresent(itemId) { _, item ->
            if (item is MaterializedItem.Stackable) {
                item.copy(quantity = item.quantity + quantityDelta) as MaterializedItem
            } else item
        }
    }
}
