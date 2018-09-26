package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.Pickaxe
import org.jetbrains.exposed.sql.Table

object EquipmentTable : Table() {
    val userId = reference("user_id", UserTable).uniqueIndex()
    val pickaxe = enumeration("pickaxe", Pickaxe::class).nullable()
}
