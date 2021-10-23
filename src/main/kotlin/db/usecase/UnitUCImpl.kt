package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.SquadTable
import com.bitwiserain.pbbg.db.repository.UnitTable
import com.bitwiserain.pbbg.db.repository.battle.BattleSessionTable
import com.bitwiserain.pbbg.domain.model.MyUnit
import com.bitwiserain.pbbg.domain.model.Squad
import com.bitwiserain.pbbg.domain.usecase.SquadInBattleException
import com.bitwiserain.pbbg.domain.usecase.UnitNotFoundException
import com.bitwiserain.pbbg.domain.usecase.UnitUC
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class UnitUCImpl(private val db: Database, private val squadTable: SquadTable) : UnitUC {
    override fun getUnit(unitId: Long): MyUnit = transaction(db) {
        UnitTable.getUnit(unitId) ?: throw UnitNotFoundException
    }

    override fun getSquad(userId: Int): Squad = transaction(db) {
        val allies = squadTable.getAllies(userId)

        Squad(allies)
    }

    override fun healSquad(userId: Int): Squad = transaction(db) {
        if (BattleSessionTable.isBattleInProgress(userId)) throw SquadInBattleException

        val allies = squadTable.getAllies(userId)

        for (unit in allies) {
            UnitTable.updateUnit(unit.id, unit.maxHeal())
        }

        return@transaction Squad(squadTable.getAllies(userId))
    }
}
