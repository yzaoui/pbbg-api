package com.bitwiserain.pbbg.app.domain.usecase

import com.bitwiserain.pbbg.app.domain.model.battle.Battle

/**
 * Generates a battle and assigns it to a user.
 */
interface GenerateBattleUC {
    /**
     * @return Newly created battle.
     *
     * @throws BattleAlreadyInProgressException when a battle is already in progress.
     * @throws NoAlliesAliveException when no allies are alive to start this battle.
     */
    operator fun invoke(userId: Int): Battle
}

class BattleAlreadyInProgressException : Exception()
class NoAlliesAliveException : Exception()
