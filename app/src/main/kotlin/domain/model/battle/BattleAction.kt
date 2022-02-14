package com.bitwiserain.pbbg.domain.model.battle

sealed class BattleAction {
    data class Attack(val targetUnitId: Long) : BattleAction()
}
