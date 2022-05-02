package com.bitwiserain.pbbg.app.domain.usecase

import com.bitwiserain.pbbg.app.domain.model.battle.Battle
import com.bitwiserain.pbbg.app.domain.model.battle.BattleAction
import com.bitwiserain.pbbg.app.domain.model.battle.BattleActionResult

interface BattleUC {
    /**
     * Generates a battle and assigns it to a user.
     *
     * @return Newly created battle.
     *
     * @throws BattleAlreadyInProgressException when a battle is already in progress.
     * @throws NoAlliesAliveException when no allies are alive to start this battle.
     */
    fun generateBattle(userId: Int): Battle

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

class BattleAlreadyInProgressException : Exception()
class NoAlliesAliveException : Exception()
class NoBattleInSessionException : Exception()
