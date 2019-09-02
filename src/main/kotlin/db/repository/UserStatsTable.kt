package com.bitwiserain.pbbg.db.repository

import com.bitwiserain.pbbg.domain.model.UserStats
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select

object UserStatsTable : Table() {
    val userId = reference("user_id", UserTable)
    val gold = long("gold").default(0)
    val miningExp = long("mining_exp").default(0)

    fun getUserStats(userId: EntityID<Int>) = select { UserStatsTable.userId.eq(userId) }
        .map { it.toUserStats() }
        .single()

    private fun ResultRow.toUserStats(): UserStats = UserStats(
        gold = this[gold],
        miningExp = this[miningExp]
    )
}
