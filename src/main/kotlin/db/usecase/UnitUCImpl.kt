package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.SquadTable
import com.bitwiserain.pbbg.domain.model.Squad
import com.bitwiserain.pbbg.domain.usecase.UnitUC
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class UnitUCImpl(private val db: Database) : UnitUC {
    override fun getSquad(userId: Int): Squad = transaction(db) {
        val allies = SquadTable.getAllies(userId)

        Squad(allies)
    }
}
