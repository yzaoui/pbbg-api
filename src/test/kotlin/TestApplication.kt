package com.bitwiserain.pbbg.test

import com.bitwiserain.pbbg.db.repository.*
import com.bitwiserain.pbbg.db.repository.battle.BattleEnemyTable
import com.bitwiserain.pbbg.db.repository.battle.BattleSessionTable
import com.bitwiserain.pbbg.db.repository.market.MarketInventoryTable
import com.bitwiserain.pbbg.db.repository.market.MarketTable
import com.bitwiserain.pbbg.db.repository.mine.MineCellTable
import com.bitwiserain.pbbg.db.repository.mine.MineSessionTable
import org.h2.Driver
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun initDatabase(): Database {
    val db = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", Driver::class.qualifiedName!!)

    transaction(db) {
        SchemaUtils.create(
            UserTable, MineSessionTable, MineCellTable, MaterializedItemTable, InventoryTable, UserStatsTable,
            UnitTable, SquadTable, BattleSessionTable, BattleEnemyTable, DexTable, MarketTable, MarketInventoryTable,
            ItemHistoryTable
        )
    }

    return db
}

fun dropDatabase(db: Database) = transaction(db) {
    SchemaUtils.drop(
        UserTable, MineSessionTable, MineCellTable, MaterializedItemTable, InventoryTable, UserStatsTable,
        UnitTable, SquadTable, BattleSessionTable, BattleEnemyTable, DexTable, MarketTable, MarketInventoryTable,
        ItemHistoryTable
    )
}

fun createTestUserAndGetId(db: Database, username: String = "testuser"): EntityID<Int> = transaction(db) {
    UserTable.createUserAndGetId(username, ByteArray(60))
}
