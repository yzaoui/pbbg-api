package com.bitwiserain.pbbg.app.domain.model.battle

sealed class BattleAction {
    data class Attack(val targetUnitId: Long) : BattleAction()
}
