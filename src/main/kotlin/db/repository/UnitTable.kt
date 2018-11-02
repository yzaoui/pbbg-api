package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.MyUnitEnum
import org.jetbrains.exposed.dao.LongIdTable

object UnitTable : LongIdTable() {
    val unit = enumeration("unit", MyUnitEnum::class)
    val hp = integer("hp")
    val maxHP = integer("max_hp")
    val atk = integer("atk")
    val exp = long("exp")
}
