package com.bitwiserain.pbbg.test.db.usecase

import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.db.usecase.BattleUCImpl
import com.bitwiserain.pbbg.test.initDatabase
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class BattleUCImplTests {
    @Test
    fun `Given a user who hasn't started a battle, when their current battle is requested, it should return null`() {
        val db = initDatabase()
        val userId = createUserAndGetId(db)
        val battleUC = BattleUCImpl(db)

        val battle = battleUC.getCurrentBattle(userId)

        assertEquals(battle, null, "User should not have a battle in session without requesting one.")
    }

    private fun createUserAndGetId(db: Database): Int = transaction(db) {
        UserTable.insertAndGetId {
            it[UserTable.username] = ""
            it[UserTable.passwordHash] = ByteArray(60)
        }.value
    }
}
