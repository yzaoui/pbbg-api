package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.SquadTable
import com.bitwiserain.pbbg.db.repository.UnitTable
import com.bitwiserain.pbbg.db.repository.battle.BattleSessionTable
import com.bitwiserain.pbbg.domain.model.Squad
import com.bitwiserain.pbbg.domain.usecase.SquadInBattleException
import com.bitwiserain.pbbg.domain.usecase.UnitUC
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class UnitUCImpl(private val db: Database) : UnitUC {
    override fun getSquad(userId: Int): Squad = transaction(db) {
        val allies = SquadTable.getAllies(userId)

        Squad(allies)
    }

    override fun healSquad(userId: Int): Squad = transaction(db) {
        if (BattleSessionTable.getBattleSessionId(userId) != null) throw SquadInBattleException()

        val allies = SquadTable.getAllies(userId)

        for (unit in allies) {
            UnitTable.updateUnit(unit.id, unit.maxHeal())
        }

        return@transaction Squad(SquadTable.getAllies(userId))
    }
}
