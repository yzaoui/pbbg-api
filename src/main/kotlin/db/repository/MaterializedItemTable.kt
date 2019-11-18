package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.ItemEnum
import com.bitwiserain.pbbg.domain.model.MaterializedItem
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

object MaterializedItemTable : LongIdTable() {
    val itemEnum = enumeration("item_enum_ordinal", ItemEnum::class)
    val quantity = integer("quantity").nullable()

    fun getItem(itemId: Long) = select { MaterializedItemTable.id.eq(itemId) }
        .singleOrNull()
        ?.toMaterializedItem()

    fun insertItemAndGetId(itemToStore: MaterializedItem): EntityID<Long> = insertAndGetId {
        it[MaterializedItemTable.itemEnum] = itemToStore.enum
        if (itemToStore is MaterializedItem.Stackable) {
            it[MaterializedItemTable.quantity] = itemToStore.quantity
        }
    }

    fun updateQuantity(itemId: EntityID<Long>, quantityDelta: Int) = update({ MaterializedItemTable.id.eq(itemId) }) {
        with (SqlExpressionBuilder) {
            it.update(MaterializedItemTable.quantity, MaterializedItemTable.quantity + quantityDelta)
        }
    }
    fun updateQuantity(itemId: Long, quantityDelta: Int) = updateQuantity(EntityID(itemId, MaterializedItemTable), quantityDelta)
}
