package com.bitwiserain.pbbg.app.db.repository

import com.bitwiserain.pbbg.app.domain.model.UserStats
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

interface UserStatsTable {

    fun createUserStats(userId: Int)

    fun getUserStats(userId: Int): UserStats

    fun updateGold(userId: Int, gold: Long)

    fun updateMiningExp(userId: Int, miningExp: Long)

    fun updateFarmingExp(userId: Int, farmingExp: Long)
}

class UserStatsTableImpl : UserStatsTable {

    object Exposed : Table(name = "UserStats") {
        val userId = reference("user_id", UserTableImpl.Exposed)
        val gold = long("gold").default(0)
        val miningExp = long("mining_exp").default(0)
        val farmingExp = long("farming_exp").default(0)
    }

    override fun createUserStats(userId: Int) {
        Exposed.insert {
            it[Exposed.userId] = EntityID(userId, UserTableImpl.Exposed)
        }
    }

    override fun getUserStats(userId: Int) = Exposed
        .select { Exposed.userId.eq(userId) }
        .map { it.toUserStats() }
        .single()

    override fun updateGold(userId: Int, gold: Long) {
        Exposed.update({ Exposed.userId.eq(userId) }) {
            it[Exposed.gold] = gold
        }
    }

    override fun updateMiningExp(userId: Int, miningExp: Long) {
        Exposed.update({ Exposed.userId.eq(userId) }) {
            it[Exposed.miningExp] = miningExp
        }
    }

    override fun updateFarmingExp(userId: Int, farmingExp: Long) {
        Exposed.update({ Exposed.userId.eq(userId) }) {
            it[Exposed.farmingExp] = farmingExp
        }
    }
}

private fun ResultRow.toUserStats(): UserStats = UserStats(
    gold = this[UserStatsTableImpl.Exposed.gold],
    miningExp = this[UserStatsTableImpl.Exposed.miningExp],
    farmingExp = this[UserStatsTableImpl.Exposed.farmingExp]
)
