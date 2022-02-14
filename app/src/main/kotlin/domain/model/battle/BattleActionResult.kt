package com.bitwiserain.pbbg.app.domain.model.battle

data class BattleActionResult(
    val battle: Battle,
    val unitEffects: Map<Long, UnitEffect>,
    val reward: BattleReward? = null
)
