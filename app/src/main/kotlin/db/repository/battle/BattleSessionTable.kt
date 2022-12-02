package com.bitwiserain.pbbg.app.db.repository.battle

import com.bitwiserain.pbbg.app.db.repository.UserTableImpl
import com.bitwiserain.pbbg.app.domain.model.battle.BattleQueue
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

interface BattleSessionTable {

    fun createBattleSessionAndGetId(userId: Int): Long

    /**
     * Get a user's battle session ID, if any.
     */
    fun getBattleSessionId(userId: Int): Long?

    fun isBattleInProgress(userId: Int): Boolean

    /**
     * Delete a battle session entry.
     */
    fun deleteBattle(battleSession: Long)

    fun getBattleQueue(battleSession: Long): BattleQueue

    fun updateBattleQueue(battleSession: Long, battleQueue: BattleQueue)
}

class BattleSessionTableImpl : BattleSessionTable {

    object Exposed : LongIdTable(name = "BattleSession") {

        val userId = reference("user_id", UserTableImpl.Exposed)
        val battleQueue = text("battle_queue")
    }

    override fun createBattleSessionAndGetId(userId: Int): Long = Exposed.insertAndGetId {
        it[Exposed.userId] = EntityID(userId, UserTableImpl.Exposed)
        it[Exposed.battleQueue] = ""
    }.value

    /**
     * Get a user's battle session ID, if any.
     */
    override fun getBattleSessionId(userId: Int): Long? = Exposed
        .select { Exposed.userId eq userId }
        .singleOrNull()
        ?.run { get(Exposed.id).value }

    override fun isBattleInProgress(userId: Int): Boolean = Exposed
        .select { Exposed.userId eq userId }.count() > 0

    /**
     * Delete a battle session entry.
     */
    override fun deleteBattle(battleSession: Long) {
        Exposed.deleteWhere { Exposed.id eq battleSession }
    }

    override fun getBattleQueue(battleSession: Long): BattleQueue {
        val turnsString = Exposed.select { Exposed.id eq battleSession}
            .single()
            .get(Exposed.battleQueue)

        return BattleQueue.fromJSON(turnsString)
    }

    override fun updateBattleQueue(battleSession: Long, battleQueue: BattleQueue) {
        Exposed.update({ Exposed.id eq battleSession }) {
            it[Exposed.battleQueue] = battleQueue.toJSON()
        }
    }
}
