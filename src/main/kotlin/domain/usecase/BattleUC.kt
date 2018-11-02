package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.Battle

interface BattleUC {
    fun getCurrentBattle(userId: Int): Battle?
    fun generateBattle(userId: Int): Battle
    fun attack(userId: Int, allyId: Long, enemyId: Long): Battle
}
