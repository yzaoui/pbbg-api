package com.bitwiserain.pbbg.db.repository.battle

import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.model.battle.BattleQueue
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

object BattleSessionTable : LongIdTable() {
    val userId = reference("user_id", UserTable)
    val battleQueue = text("battle_queue")

    fun createBattleSessionAndGetId(userId: Int): Long = insertAndGetId {
        it[BattleSessionTable.userId] = EntityID(userId, UserTable)
        it[BattleSessionTable.battleQueue] = ""
    }.value

    /**
     * Get a user's battle session ID, if any.
     */
    fun getBattleSessionId(userId: Int): Long? {
        return select { BattleSessionTable.userId eq userId }
            .singleOrNull()
            ?.run { get(BattleSessionTable.id).value }
    }

    fun isBattleInProgress(userId: Int): Boolean {
        return select { BattleSessionTable.userId eq userId }.count() > 0
    }

    /**
     * Delete a battle session entry.
     */
    fun deleteBattle(battleSession: Long) {
        deleteWhere { id eq battleSession }
    }

    fun getBattleQueue(battleSession: Long): BattleQueue {
        val turnsString = select { id eq battleSession}
            .single()
            .get(battleQueue)

        return BattleQueue.fromJSON(turnsString)
    }

    fun updateBattleQueue(battleSession: Long, battleQueue: BattleQueue) {
        update({ id eq battleSession }) {
            it[this.battleQueue] = battleQueue.toJSON()
        }
    }
}
