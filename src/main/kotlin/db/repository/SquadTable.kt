package com.bitwiserain.pbbg.db.repository

import org.jetbrains.exposed.dao.LongIdTable

object SquadTable : LongIdTable() {
    val user = reference("user_id", UserTable)
    val unit = reference("unit_id", UnitTable)
}
