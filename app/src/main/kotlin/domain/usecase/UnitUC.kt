package com.bitwiserain.pbbg.app.domain.usecase

import com.bitwiserain.pbbg.app.domain.model.MyUnit
import com.bitwiserain.pbbg.app.domain.model.Squad

interface UnitUC {
    /**
     * @throws UnitNotFoundException when unit with this ID does not exist.
     */
    fun getUnit(unitId: Long): MyUnit

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

object UnitNotFoundException : Exception()
object SquadInBattleException : Exception()
