package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.mine.Mine
import com.bitwiserain.pbbg.domain.model.mine.MineActionResult
import com.bitwiserain.pbbg.domain.model.mine.MineType

interface MiningUC {
    fun getMine(userId: Int): Mine?

    /**
     * Generate a new mine and enter it.
     *
     * @param userId The user's ID.
     * @param mineType The kind of mine to generate.
     * @param width The width in cells of the mine to generate.
     * @param height The height in cells of the mine to generate
     *
     * @throws UnfulfilledLevelRequirementException when
     */
    fun generateMine(userId: Int, mineType: MineType, width: Int, height: Int): Mine

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

class UnfulfilledLevelRequirementException : IllegalStateException()

class NoEquippedPickaxeException : IllegalStateException()
class NotInMineSessionException : IllegalStateException()
