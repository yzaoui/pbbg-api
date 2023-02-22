package com.bitwiserain.pbbg.app.domain.usecase

import com.bitwiserain.pbbg.app.domain.model.mine.MineActionResult

interface MiningUC {
    /**
     * @throws NoEquippedPickaxeException when mining cannot occur due to the lack of an equipped pickaxe.
     * @throws NotInMineSessionException when mining cannot occur due to the lack of an existing mining session.
     */
    fun submitMineAction(userId: Int, x: Int, y: Int): MineActionResult
}

class NoEquippedPickaxeException : Exception()
class NotInMineSessionException : Exception()
