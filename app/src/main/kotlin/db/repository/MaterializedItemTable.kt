package com.bitwiserain.pbbg.app.db.repository

import com.bitwiserain.pbbg.app.domain.model.ItemEnum
import com.bitwiserain.pbbg.app.domain.model.MaterializedItem
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

interface MaterializedItemTable {

    fun getItem(itemId: Long): MaterializedItem?

    fun insertItemAndGetId(itemToStore: MaterializedItem): Long

    fun updateQuantity(itemId: Long, quantityDelta: Int)
}

class MaterializedItemTableImpl : MaterializedItemTable {

    object Exposed : LongIdTable(name = "MaterializedItem") {
        val itemEnum = enumeration("item_enum_ordinal", ItemEnum::class)
        val quantity = integer("quantity").nullable()
    }

    override fun getItem(itemId: Long) = Exposed
        .select { Exposed.id.eq(itemId) }
        .singleOrNull()
        ?.toMaterializedItem()

    override fun insertItemAndGetId(itemToStore: MaterializedItem): Long = Exposed.insertAndGetId {
        it[Exposed.itemEnum] = itemToStore.enum
        if (itemToStore is MaterializedItem.Stackable) {
            it[Exposed.quantity] = itemToStore.quantity
        }
    }.value

    override fun updateQuantity(itemId: Long, quantityDelta: Int) {
        Exposed.update({ Exposed.id.eq(itemId) }) {
            with (SqlExpressionBuilder) {
                it.update(Exposed.quantity, Exposed.quantity + quantityDelta)
            }
        }
    }
}
