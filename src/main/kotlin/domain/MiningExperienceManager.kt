package com.bitwiserain.pbbg.domain

import com.bitwiserain.pbbg.domain.model.LevelProgress
import com.bitwiserain.pbbg.domain.model.LevelUp

object MiningExperienceManager {
    private val levels = listOf(20, 55, 85, 120, 160, 205, 255, 310, 370, 435, 505, 580)

    fun getLevelProgress(absoluteExp: Int): LevelProgress {
        var prevLevelAbsoluteExp = 0
        for ((i, cap) in levels.withIndex()) {
            if (absoluteExp < cap) {
                return LevelProgress(
                    level = i + 1,
                    absoluteExp = absoluteExp,
                    absoluteExpCurrentLevel = prevLevelAbsoluteExp,
                    absoluteExpNextLevel = prevLevelAbsoluteExp + cap
                )
            }

            prevLevelAbsoluteExp = cap
        }

        return LevelProgress(
            level = levels.size + 1,
            absoluteExp = levels.last(),
            absoluteExpCurrentLevel = levels.last(),
            absoluteExpNextLevel = levels.last()
        )
    }

    fun getLevelUpResults(prevLevel: Int, newLevel: Int): List<LevelUp> {
        return ((prevLevel + 1)..newLevel).map { LevelUp(it) }
    }
}
