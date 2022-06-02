package com.bitwiserain.pbbg.app.db.usecase

import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.SquadTable
import com.bitwiserain.pbbg.app.db.repository.UnitTable
import com.bitwiserain.pbbg.app.db.repository.battle.BattleSessionTable
import com.bitwiserain.pbbg.app.domain.model.MyUnit
import com.bitwiserain.pbbg.app.domain.model.Squad
import com.bitwiserain.pbbg.app.domain.usecase.SquadInBattleException
import com.bitwiserain.pbbg.app.domain.usecase.UnitNotFoundException
import com.bitwiserain.pbbg.app.domain.usecase.UnitUC

class UnitUCImpl(
    private val transaction: Transaction,
    private val battleSessionTable: BattleSessionTable,
    private val squadTable: SquadTable,
    private val unitTable: UnitTable
) : UnitUC {

    override fun getUnit(unitId: Long): MyUnit = transaction {
        unitTable.getUnit(unitId) ?: throw UnitNotFoundException
    }

    override fun getSquad(userId: Int): Squad = transaction {
        val allies = squadTable.getAllies(userId)

        Squad(allies)
    }

    override fun healSquad(userId: Int): Squad = transaction {
        if (battleSessionTable.isBattleInProgress(userId)) throw SquadInBattleException

        val allies = squadTable.getAllies(userId)

        for (unit in allies) {
            unitTable.updateUnit(unit.id, unit.maxHeal())
        }

        return@transaction Squad(squadTable.getAllies(userId))
    }
}
