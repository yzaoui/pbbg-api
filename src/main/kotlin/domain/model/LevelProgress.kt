package com.bitwiserain.pbbg.domain.model

data class LevelProgress(
    val level: Int,
    val expThisLevel: Int,
    val totalExpToNextLevel: Int,
    val absoluteExp: Int
)
