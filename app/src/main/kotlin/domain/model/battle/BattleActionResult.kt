package com.bitwiserain.pbbg.domain.model.battle

data class BattleActionResult(
    val battle: Battle,
    val unitEffects: Map<Long, UnitEffect>,
    val reward: BattleReward? = null
)
