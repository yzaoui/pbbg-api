package com.bitwiserain.pbbg.domain.model

/**
 * Represent
 *
 * @property level Current level.
 * @property absoluteExp Total experience since level 1.
 * @property absoluteExpCurrentLevel Total experience to reach current level.
 * @property absoluteExpNextLevel Total experience to reach next level.
 */
data class LevelProgress(
    val level: Int,
    val absoluteExp: Int,
    private val absoluteExpCurrentLevel: Int,
    private val absoluteExpNextLevel: Int
) {
    /**
     * The current experience amount relative the previous level up threshold.
     */
    val relativeExp: Int
        get() = absoluteExp - absoluteExpCurrentLevel

    /**
     * The experience to the next level up relative to the previous level up threshold.
     */
    val relativeExpNextLevel: Int
        get() = absoluteExpNextLevel - absoluteExpCurrentLevel
}
