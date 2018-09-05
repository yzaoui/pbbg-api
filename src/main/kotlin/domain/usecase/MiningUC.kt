package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.mine.Mine
import com.bitwiserain.pbbg.domain.model.mine.MineActionResult

interface MiningUC {
    fun getMine(userId: Int): Mine?
    fun generateMine(userId: Int, width: Int, height: Int): Mine
    fun submitMineAction(userId: Int, x: Int, y: Int): List<MineActionResult>?
}
