package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.ItemEnum
import com.bitwiserain.pbbg.domain.model.MaterializedItem
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update

object MaterializedItemTable : LongIdTable() {
    val itemEnum = enumeration("item_enum_ordinal", ItemEnum::class)
    val quantity = integer("quantity").nullable()

    fun insertItemAndGetId(itemToStore: MaterializedItem): EntityID<Long> = insertAndGetId {
        it[MaterializedItemTable.itemEnum] = itemToStore.enum
        if (itemToStore is MaterializedItem.Stackable) {
            it[MaterializedItemTable.quantity] = itemToStore.quantity
        }
    }

    fun updateQuantity(itemId: EntityID<Long>, quantity: Int) = update({ MaterializedItemTable.id.eq(itemId) }) {
        with (SqlExpressionBuilder) {
            it.update(MaterializedItemTable.quantity, MaterializedItemTable.quantity + quantity)
        }
    }
}
