package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.UserStats
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*

object UserStatsTable : Table() {
    val userId = reference("user_id", UserTable)
    val gold = long("gold").default(0)
    val miningExp = long("mining_exp").default(0)
    val farmingExp = long("farming_exp").default(0)

    fun createUserStats(userId: Int) = insert {
        it[UserStatsTable.userId] = EntityID(userId, UserTable)
    }

    fun getUserStats(userId: Int) = select { UserStatsTable.userId.eq(userId) }
        .map { it.toUserStats() }
        .single()

    fun updateGold(userId: Int, gold: Long) = update({ UserStatsTable.userId.eq(userId) }) {
        it[UserStatsTable.gold] = gold
    }

    fun updateMiningExp(userId: Int, miningExp: Long) = update({ UserStatsTable.userId.eq(userId) }) {
        it[UserStatsTable.miningExp] = miningExp
    }

    private fun ResultRow.toUserStats(): UserStats = UserStats(
        gold = this[gold],
        miningExp = this[miningExp],
        farmingExp = this[farmingExp]
    )
}
