package com.bitwiserain.pbbg.test

import com.bitwiserain.pbbg.db.repository.*
import com.bitwiserain.pbbg.db.repository.battle.BattleEnemyTable
import com.bitwiserain.pbbg.db.repository.battle.BattleSessionTable
import org.h2.Driver
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun initDatabase(): Database {
    val db = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", Driver::class.qualifiedName!!)

    transaction(db) {
        SchemaUtils.create(
            UserTable, MineSessionTable, MineCellTable, EquipmentTable, InventoryTable, UserStatsTable,
            UnitTable, SquadTable, BattleSessionTable, BattleEnemyTable
        )
    }

    return db
}
