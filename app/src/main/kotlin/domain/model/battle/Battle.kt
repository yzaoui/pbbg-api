package com.bitwiserain.pbbg.app.domain.model.battle

import com.bitwiserain.pbbg.app.domain.model.MyUnit

data class Battle(
    val allies: List<MyUnit>,
    val enemies: List<MyUnit>,
    val battleQueue: BattleQueue
) {
    fun contains(unitId: Long): Boolean {
        return (allies + enemies).any { it.id == unitId }
    }
}
