package com.bitwiserain.pbbg.app.domain.usecase.mine

import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.UserStatsTable
import com.bitwiserain.pbbg.app.db.repository.mine.MineCellTable
import com.bitwiserain.pbbg.app.db.repository.mine.MineSessionTable
import com.bitwiserain.pbbg.app.domain.MiningExperienceManager
import com.bitwiserain.pbbg.app.domain.model.mine.Mine
import com.bitwiserain.pbbg.app.domain.model.mine.MineEntity
import com.bitwiserain.pbbg.app.domain.model.mine.MineType
import kotlin.random.Random

/**
 * Generate a new mine and enter it.
 */
fun interface GenerateMine : (Int, Int, Int, Int) -> Mine {
    /**
     * @param userId The user's ID.
     * @param mineTypeId The kind of mine to generate.
     * @param width The width in cells of the mine to generate.
     * @param height The height in cells of the mine to generate.
     *
     * @throws AlreadyInMineException when already in a mine.
     * @throws InvalidMineTypeIdException when [mineTypeId] does not map to a valid [MineType].
     * @throws UnfulfilledLevelRequirementException when minimum mining level requirement is not met.
     */
    override fun invoke(userId: Int, mineTypeId: Int, width: Int, height: Int): Mine
}

class AlreadyInMineException : Exception()
class InvalidMineTypeIdException(val id: Int) : Exception()
class UnfulfilledLevelRequirementException(val currentLevel: Int, val requiredMinimumLevel: Int) : Exception()

class GenerateMineImpl(
    private val transaction: Transaction,
    private val mineCellTable: MineCellTable,
    private val mineSessionTable: MineSessionTable,
    private val userStatsTable: UserStatsTable,
) : GenerateMine {

    override fun invoke(userId: Int, mineTypeId: Int, width: Int, height: Int): Mine = transaction {
        // Don't generate mine when already in one
        if (mineSessionTable.getSession(userId) != null) throw AlreadyInMineException()

        val mineType = try {
            MineType.values()[mineTypeId]
        } catch (e: ArrayIndexOutOfBoundsException) {
            throw InvalidMineTypeIdException(id = mineTypeId)
        }

        val itemEntries = mutableMapOf<Pair<Int, Int>, MineEntity>()
        (0 until height).forEach { y ->
            (0 until width).forEach { x ->
                mineType.rollForMineEntity(Random.nextFloat())?.let {
                    itemEntries[x to y] = it
                }
            }
        }

        val userMiningExp = userStatsTable.getUserStats(userId).miningExp

        val userMiningProgress = MiningExperienceManager.getLevelProgress(userMiningExp)

        if (userMiningProgress.level < mineType.minLevel) throw UnfulfilledLevelRequirementException(
            currentLevel = userMiningProgress.level,
            requiredMinimumLevel = mineType.minLevel
        )

        val mineSessionId = mineSessionTable.insertSessionAndGetId(userId, width, height, mineType)

        mineCellTable.insertCells(mineSessionId, itemEntries)

        return@transaction Mine(width, height, itemEntries, mineType)
    }
}
