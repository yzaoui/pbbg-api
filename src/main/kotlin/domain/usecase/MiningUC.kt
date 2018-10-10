package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.mine.Mine
import com.bitwiserain.pbbg.domain.model.mine.MineActionResult
import com.bitwiserain.pbbg.domain.model.mine.MineType

interface MiningUC {
    /**
     * Get the user's current mine, or null if not currently in one.
     */
    fun getMine(userId: Int): Mine?

    /**
     * Generate a new mine and enter it.
     *
     * @param userId The user's ID.
     * @param mineTypeId The kind of mine to generate.
     * @param width The width in cells of the mine to generate.
     * @param height The height in cells of the mine to generate.
     *
     * @throws InvalidMineTypeIdException when [mineTypeId] does not map to a valid [MineType].
     * @throws UnfulfilledLevelRequirementException when minimum mining level requirement is not met.
     */
    fun generateMine(userId: Int, mineTypeId: Int, width: Int, height: Int): Mine

    /**
     * Exit the mine currently in session, if any.
     */
    fun exitMine(userId: Int)

    /**
     * @throws NoEquippedPickaxeException when mining cannot occur due to the lack of an equipped pickaxe.
     * @throws NotInMineSessionException when mining cannot occur due to the lack of an existing mining session.
     */
    fun submitMineAction(userId: Int, x: Int, y: Int): MineActionResult
}

class InvalidMineTypeIdException(val id: Int) : Exception()
class UnfulfilledLevelRequirementException(val currentLevel: Int, val requiredMinimumLevel: Int) : Exception()

class NoEquippedPickaxeException : Exception()
class NotInMineSessionException : Exception()
