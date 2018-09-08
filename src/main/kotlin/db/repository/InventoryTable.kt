package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.ItemEnum
import org.jetbrains.exposed.sql.Table

object InventoryTable : Table() {
    val userId = reference("user_id", UserTable)
    val item = enumeration("base_item_ordinal", ItemEnum::class.java)
    val quantity = integer("quantity").nullable()
}
