package com.bitwiserain.pbbg.view.model

data class MyUnitJSON(
    val id: Long,
    val name: String,
    val baseUnitId: Int,
    val hp: Int,
    val maxHP: Int,
    val atk: Int,
    val levelProgress: LevelProgressJSON,
    val idleAnimationURL: String,
    val iconURL: String
)
