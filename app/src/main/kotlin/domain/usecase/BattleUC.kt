package com.bitwiserain.pbbg.app.domain.usecase

import com.bitwiserain.pbbg.app.domain.model.battle.BattleAction
import com.bitwiserain.pbbg.app.domain.model.battle.BattleActionResult

interface BattleUC {
    /**
     * Plays an ally unit's turn.
     *
     * @return Updated battle.
     *
     * @throws NoBattleInSessionException when user has no battle in session.
     */
    fun allyTurn(userId: Int, action: BattleAction): BattleActionResult

    /**
     * Plays an enemy (A.I.) unit's turn.
     *
     * @return Updated battle.
     *
     * @throws NoBattleInSessionException when user has no battle in session.
     */
    fun enemyTurn(userId: Int): BattleActionResult
}

class NoBattleInSessionException : Exception()
