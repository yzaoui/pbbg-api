package com.bitwiserain.pbbg.db.repository.battle

import com.bitwiserain.pbbg.db.repository.UserTable
import org.jetbrains.exposed.dao.LongIdTable

object BattleSessionTable : LongIdTable() {
    val userId = reference("user_id", UserTable)
}
