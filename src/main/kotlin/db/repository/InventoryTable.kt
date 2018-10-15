package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.ItemEnum
import org.jetbrains.exposed.dao.IntIdTable

object InventoryTable : IntIdTable() {
    val userId = reference("user_id", UserTable)
    val item = enumeration("base_item_ordinal", ItemEnum::class)
    val quantity = integer("quantity").nullable()
    val equipped = bool("equipped").nullable()
}
