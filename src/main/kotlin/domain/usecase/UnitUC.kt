package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.Squad

interface UnitUC {
    /**
     * User's squad.
     */
    fun getSquad(userId: Int): Squad

    /**
     * Heals the user's out-of-battle squad.
     *
     * @throws SquadInBattleException when a battle is in session. Healing mid-battle is forbidden.
     */
    fun healSquad(userId: Int): Squad
}

class SquadInBattleException : Exception()
