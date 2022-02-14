package com.bitwiserain.pbbg.app.domain

import com.bitwiserain.pbbg.app.domain.model.LevelUp
import com.bitwiserain.pbbg.app.domain.model.mine.MineType

object MiningExperienceManager : ExperienceManager() {
    override val levels = listOf(20L, 55L, 85L, 120L, 160L, 205L, 255L, 310L, 370L, 435L, 505L, 580L)

    fun getLevelUpResults(prevLevel: Int, newLevel: Int): List<LevelUp> {
        return ((prevLevel + 1)..newLevel).map {
            // TODO: Make this more robust
            if (it == MineType.MODERATE.minLevel) {
                LevelUp(it, "Gained access to ${MineType.MODERATE.friendlyName}.")
            } else {
                LevelUp(it)
            }
        }
    }
}
