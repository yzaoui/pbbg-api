package com.bitwiserain.pbbg.app.domain

import com.bitwiserain.pbbg.app.domain.model.LevelProgress

abstract class ExperienceManager {
    /**
     * A list where every value represents the minimum absolute amount of experience to reach the next level from level 1.
     */
    protected abstract val levels: List<Long>

    fun getLevelProgress(absoluteExp: Long): LevelProgress {
        // Previous threshold
        var prevLevelAbsoluteExp = 0L

        for ((i, absoluteExpToLevelUp) in levels.withIndex()) {
            // Since levels start at 1 not 0
            val level = i + 1

            if (absoluteExp < absoluteExpToLevelUp) {
                return LevelProgress(
                    level = level,
                    absoluteExp = absoluteExp,
                    absoluteExpCurrentLevel = prevLevelAbsoluteExp,
                    absoluteExpNextLevel = absoluteExpToLevelUp
                )
            }

            prevLevelAbsoluteExp = absoluteExpToLevelUp
        }

        return LevelProgress(
            level = levels.size + 1,
            absoluteExp = levels.last(),
            absoluteExpCurrentLevel = levels.last(),
            absoluteExpNextLevel = levels.last()
        )
    }
}
