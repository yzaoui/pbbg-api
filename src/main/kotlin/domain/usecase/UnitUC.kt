package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.Squad

interface UnitUC {
    fun getSquad(userId: Int): Squad
    /**
     * @throws SquadInBattleException when a battle is in session. Healing mid-battle is forbidden.
     */
    fun healSquad(userId: Int): Squad
}

class SquadInBattleException : Exception()
