package com.bitwiserain.pbbg.app.domain.model.battle

sealed class UnitEffect {
    data class Health(val delta: Int) : UnitEffect()
}
