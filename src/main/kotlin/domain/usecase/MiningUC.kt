package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.mine.Mine
import com.bitwiserain.pbbg.domain.model.mine.MineActionResult

interface MiningUC {
    fun getMine(userId: Int): Mine?
    fun generateMine(userId: Int, width: Int, height: Int): Mine

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

class NoEquippedPickaxeException : IllegalStateException()
class NotInMineSessionException : IllegalStateException()
