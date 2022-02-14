package com.bitwiserain.pbbg.app.domain

import kotlin.math.ceil

object BattleManager {
    fun calculateDamage(attack: Int, defence: Int): Int {
        return ceil(attack * attack / (attack + defence).toDouble()).toInt()
    }
}
