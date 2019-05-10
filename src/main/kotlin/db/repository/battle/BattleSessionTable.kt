package com.bitwiserain.pbbg.db.repository.battle

import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.model.battle.BattleQueue
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

object BattleSessionTable : LongIdTable() {
    val userId = reference("user_id", UserTable)
    val battleQueue = text("battle_queue")

    /**
     * Get a user's battle session ID, if any.
     */
    fun getBattleSessionId(userId: Int): EntityID<Long>? {
        return select { BattleSessionTable.userId eq userId }
            .singleOrNull()
            ?.get(id)
    }

    /**
     * Delete a battle session entry.
     */
    fun deleteBattle(battleSession: EntityID<Long>) {
        deleteWhere { id eq battleSession }
    }

    fun getBattleQueue(battleSession: EntityID<Long>): BattleQueue {
        val turnsString = select { id eq battleSession}
            .single()
            .get(battleQueue)

        return BattleQueue.fromJSON(turnsString)
    }

    fun updateBattleQueue(battleSession: EntityID<Long>, battleQueue: BattleQueue) {
        update({ id eq battleSession }) {
            it[this.battleQueue] = battleQueue.toJSON()
        }
    }
}
