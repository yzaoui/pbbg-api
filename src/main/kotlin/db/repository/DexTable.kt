package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.ItemEnum
import org.jetbrains.exposed.sql.Table

object DexTable : Table() {
    val userId = reference("user_id", UserTable)
    val item = enumeration("base_item_ordinal", ItemEnum::class)
}
