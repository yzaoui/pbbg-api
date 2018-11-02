package com.bitwiserain.pbbg.db.repository.battle

import com.bitwiserain.pbbg.db.repository.UserTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select

object BattleSessionTable : LongIdTable() {
    val userId = reference("user_id", UserTable)

    /**
     * Get a user's battle session ID, if any.
     */
    fun getBattleSessionId(userId: Int): EntityID<Long>? {
        return select { BattleSessionTable.userId eq userId }
            .singleOrNull()
            ?.get(BattleSessionTable.id)
    }

    /**
     * Delete a battle session entry.
     */
    fun deleteBattle(battleSession: EntityID<Long>) {
        deleteWhere { BattleSessionTable.id eq battleSession }
    }
}
