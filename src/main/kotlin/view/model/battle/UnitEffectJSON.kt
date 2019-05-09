package com.bitwiserain.pbbg.view.model.battle

sealed class UnitEffectJSON(val type: String) {
    data class HealthJSON(val delta: Int) : UnitEffectJSON("health")
}
