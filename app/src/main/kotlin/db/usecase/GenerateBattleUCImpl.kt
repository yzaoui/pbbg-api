package com.bitwiserain.pbbg.app.db.usecase

import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.SquadTable
import com.bitwiserain.pbbg.app.db.repository.UnitTable
import com.bitwiserain.pbbg.app.db.repository.battle.BattleEnemyTable
import com.bitwiserain.pbbg.app.db.repository.battle.BattleSessionTable
import com.bitwiserain.pbbg.app.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.app.domain.model.battle.Battle
import com.bitwiserain.pbbg.app.domain.model.battle.BattleQueue
import com.bitwiserain.pbbg.app.domain.model.battle.Turn
import com.bitwiserain.pbbg.app.domain.usecase.BattleAlreadyInProgressException
import com.bitwiserain.pbbg.app.domain.usecase.GenerateBattleUC
import com.bitwiserain.pbbg.app.domain.usecase.NoAlliesAliveException

class GenerateBattleUCImpl(
    private val transaction: Transaction,
    private val battleEnemyTable: BattleEnemyTable,
    private val battleSessionTable: BattleSessionTable,
    private val squadTable: SquadTable,
    private val unitTable: UnitTable,
) : GenerateBattleUC {

    override fun invoke(userId: Int): Battle = transaction {
        if (battleSessionTable.isBattleInProgress(userId)) throw BattleAlreadyInProgressException()

        val allies = squadTable.getAllies(userId)

        // There must be allies alive to start a battle
        if (allies.none { it.alive }) throw NoAlliesAliveException()

        val battleSession = battleSessionTable.createBattleSessionAndGetId(userId)

        val newEnemies = mutableListOf<UnitTable.UnitForm>()
        // Add 1-3 new enemies
        for (i in 0..<(1..3).random()) {
            newEnemies.add(UnitTable.UnitForm(MyUnitEnum.entries.random(), (7..14).random(), (5..7).random(), (5..7).random(), (6..8).random(), (4..7).random()))
        }
        // TODO: There's gotta be a way to do this in batch :/
        for (enemy in newEnemies) {
            // Create enemy unit in unit table
            val unitId = unitTable.insertUnitAndGetId(enemy)
            // Connect newly created enemy to this battle session
            battleEnemyTable.insertEnemy(battleSession, unitId)
        }

        val enemies = battleEnemyTable.getEnemies(battleSession)

        // TODO: Temporary function to get around test coverage failing when sortedByDescending is involved directly
        fun List<Turn>.sortedByDescendingCounter() = sortedByDescending { it.counter }

        val battleQueue = BattleQueue(
            turns = (allies + enemies)
                .filter { it.alive }
                .map { Turn(it.id, (0..99).random()) }
                .sortedByDescendingCounter()
        )

        battleSessionTable.updateBattleQueue(battleSession, battleQueue)

        return@transaction Battle(allies = allies, enemies = enemies, battleQueue = battleQueue)
    }
}
