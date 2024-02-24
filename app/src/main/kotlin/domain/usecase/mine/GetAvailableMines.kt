package com.bitwiserain.pbbg.app.domain.usecase.mine

import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.UserStatsTable
import com.bitwiserain.pbbg.app.domain.MiningExperienceManager
import com.bitwiserain.pbbg.app.domain.model.mine.AvailableMines
import com.bitwiserain.pbbg.app.domain.model.mine.MineType

/**
 * Gets the mines the user currently has access to.
 */
fun interface GetAvailableMines : (Int) -> AvailableMines {
    /**
     * @param userId The user's ID.
     */
    override fun invoke(userId: Int): AvailableMines
}

class GetAvailableMinesImpl(
    private val transaction: Transaction,
    private val userStatsTable: UserStatsTable,
) : GetAvailableMines {

    override fun invoke(userId: Int): AvailableMines {
        val userMiningLevel = transaction {
            userStatsTable.getUserStats(userId).miningExp
        }.let { exp ->
            MiningExperienceManager.getLevelProgress(exp)
        }.level

        val mines = mutableListOf<MineType>()
        var nextUnlockLevel: Int? = null

        for (mine in MineType.entries) {
            if (userMiningLevel >= mine.minLevel) {
                mines.add(mine)
            } else {
                nextUnlockLevel = mine.minLevel
                break
            }
        }

        return AvailableMines(mines, nextUnlockLevel)
    }
}
