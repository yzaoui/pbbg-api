package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.battle.Battle
import com.bitwiserain.pbbg.domain.model.battle.BattleAction

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

    /**
     * Plays an ally unit's turn.
     *
     * @return Updated battle.
     *
     * @throws NoBattleInSessionException
     */
    fun allyTurn(userId: Int, action: BattleAction): Battle

    /**
     * Plays an enemy (A.I.) unit's turn.
     *
     * @return Updated battle.
     *
     * @throws NoBattleInSessionException
     */
    fun enemyTurn(userId: Int): Battle
}

class NoBattleInSessionException : Exception()
