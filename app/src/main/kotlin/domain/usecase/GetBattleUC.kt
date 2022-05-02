package com.bitwiserain.pbbg.app.domain.usecase

import com.bitwiserain.pbbg.app.domain.model.battle.Battle

/**
 * Gets the user's current battle.
 */
interface GetBattleUC {
    /**
     * @return The battle in session, or null if none.
     */
    operator fun invoke(userId: Int): Battle?
}
