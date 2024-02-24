package com.bitwiserain.pbbg.app.domain.usecase.mine

import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.UserStatsTable
import com.bitwiserain.pbbg.app.db.repository.mine.MineCellTable
import com.bitwiserain.pbbg.app.db.repository.mine.MineSessionTable
import com.bitwiserain.pbbg.app.domain.MiningExperienceManager
import com.bitwiserain.pbbg.app.domain.model.mine.Mine
import com.bitwiserain.pbbg.app.domain.model.mine.MineType
import com.bitwiserain.pbbg.app.domain.usecase.mine.GenerateMine.Result
import kotlin.random.Random

/**
 * Generate a new mine and enter it.
 */
fun interface GenerateMine : (Int, Int, Int, Int) -> Result {
    /**
     * @param userId The user's ID.
     * @param mineTypeId The kind of mine to generate.
     * @param width The width in cells of the mine to generate.
     * @param height The height in cells of the mine to generate.
     */
    override fun invoke(userId: Int, mineTypeId: Int, width: Int, height: Int): Result

    sealed class Result {
        /**
         * Mine was successfully generated.
         */
        data class SuccessfullyGenerated(val mine: Mine) : Result()

        /**
         * User is already in a mine.
         */
        data object AlreadyInMine : Result()

        /**
         * Given mine type ID does not map to a valid [MineType].
         */
        data object InvalidMineTypeId : Result()

        /**
         * Minimum mining level requirement is not met for the given mine type.
         */
        data class UnfulfilledLevelRequirement(val currentLevel: Int, val requiredMinimumLevel: Int) : Result()
    }
}

class GenerateMineImpl(
    private val transaction: Transaction,
    private val mineCellTable: MineCellTable,
    private val mineSessionTable: MineSessionTable,
    private val userStatsTable: UserStatsTable,
) : GenerateMine {

    override fun invoke(userId: Int, mineTypeId: Int, width: Int, height: Int): Result = transaction {
        // Don't generate mine when already in one
        if (mineSessionTable.getSession(userId) != null) return@transaction Result.AlreadyInMine

        val mineType = MineType.entries.getOrNull(mineTypeId) ?: return@transaction Result.InvalidMineTypeId

        val itemEntries = buildMap {
            (0..<height).forEach { y ->
                (0..<width).forEach { x ->
                    mineType.rollForMineEntity(Random.nextFloat())?.let {
                        this[x to y] = it
                    }
                }
            }
        }

        val userMiningExp = userStatsTable.getUserStats(userId).miningExp

        val userMiningProgress = MiningExperienceManager.getLevelProgress(userMiningExp)

        if (userMiningProgress.level < mineType.minLevel) return@transaction Result.UnfulfilledLevelRequirement(
            currentLevel = userMiningProgress.level,
            requiredMinimumLevel = mineType.minLevel
        )

        val mineSessionId = mineSessionTable.insertSessionAndGetId(userId, width, height, mineType)

        mineCellTable.insertCells(mineSessionId, itemEntries)

        return@transaction Result.SuccessfullyGenerated(Mine(width, height, itemEntries, mineType))
    }
}
