package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.Battle

interface BattleUC {
    /**
     * Gets the user's current battle.
     *
     * @return The battle in session, or null if none.
     */
    fun getCurrentBattle(userId: Int): Battle?

    /**
     * Generates a battle and assigns it to a user.
     *
     * @return Newly created battle.
     */
    fun generateBattle(userId: Int): Battle
    fun attack(userId: Int, allyId: Long, enemyId: Long): Battle
}
