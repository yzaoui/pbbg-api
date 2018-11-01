package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.CharUnitEnum
import org.jetbrains.exposed.dao.LongIdTable

object UnitTable : LongIdTable() {
    val unit = enumeration("unit", CharUnitEnum::class)
    val hp = integer("hp")
    val maxHP = integer("max_hp")
    val atk = integer("atk")
    val exp = long("exp")
}
