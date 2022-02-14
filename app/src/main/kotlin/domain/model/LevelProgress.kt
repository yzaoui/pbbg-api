package com.bitwiserain.pbbg.app.domain.model

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
    val absoluteExp: Long,
    private val absoluteExpCurrentLevel: Long,
    private val absoluteExpNextLevel: Long
) {
    /**
     * The current experience amount relative the previous level up threshold.
     */
    val relativeExp: Long
        get() = absoluteExp - absoluteExpCurrentLevel

    /**
     * The experience to the next level up relative to the previous level up threshold.
     */
    val relativeExpNextLevel: Long
        get() = absoluteExpNextLevel - absoluteExpCurrentLevel
}
