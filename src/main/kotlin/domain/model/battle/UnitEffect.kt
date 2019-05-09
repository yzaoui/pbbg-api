package com.bitwiserain.pbbg.domain.model.battle

sealed class UnitEffect {
    data class Health(val delta: Int) : UnitEffect()
}
