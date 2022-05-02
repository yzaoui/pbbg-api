package com.bitwiserain.pbbg.app.db.usecase

import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.SquadTable
import com.bitwiserain.pbbg.app.db.repository.battle.BattleEnemyTable
import com.bitwiserain.pbbg.app.db.repository.battle.BattleSessionTable
import com.bitwiserain.pbbg.app.domain.model.battle.Battle
import com.bitwiserain.pbbg.app.domain.usecase.GetBattleUC

class GetBattleUCImpl(
    private val transaction: Transaction,
    private val battleEnemyTable: BattleEnemyTable,
    private val battleSessionTable: BattleSessionTable,
    private val squadTable: SquadTable,
) : GetBattleUC {

    override fun invoke(userId: Int): Battle? = transaction {
        val battleSession = battleSessionTable.getBattleSessionId(userId) ?: return@transaction null

        Battle(
            allies = squadTable.getAllies(userId),
            enemies = battleEnemyTable.getEnemies(battleSession),
            battleQueue = battleSessionTable.getBattleQueue(battleSession)
        )
    }
}
