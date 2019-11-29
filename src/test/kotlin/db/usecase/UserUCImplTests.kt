package com.bitwiserain.pbbg.test.db.usecase

import com.bitwiserain.pbbg.db.repository.Joins
import com.bitwiserain.pbbg.db.repository.SquadTable
import com.bitwiserain.pbbg.db.repository.UserStatsTable
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.db.usecase.UserUCImpl
import com.bitwiserain.pbbg.domain.model.BaseItem
import com.bitwiserain.pbbg.domain.model.ItemEnum
import com.bitwiserain.pbbg.domain.model.MaterializedItem
import com.bitwiserain.pbbg.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.test.dropDatabase
import com.bitwiserain.pbbg.test.initDatabase
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class UserUCImplTests {
    private val db = initDatabase()
    private val userUC: UserUC = UserUCImpl(db)

    @AfterEach
    fun dropDatabase() {
        dropDatabase(db)
    }

    @Nested
    inner class SuccessfulRegistration {
        @Test
        fun `When registering a new user, the user should have 0 gold and 0 mining exp`() {
            val userId = userUC.registerUser("username", "password")

            val stats = transaction(db) { UserStatsTable.getUserStats(EntityID(userId, UserTable)) }

            assertEquals(0, stats.gold)
            assertEquals(0, stats.miningExp)
        }

        @Test
        fun `When registering a new user, the user's inventory should only contain 1 ice pick`() {
            val userId = userUC.registerUser("username", "password")

            val inventoryItems = transaction(db) { Joins.getInventoryItems(EntityID(userId, UserTable)) }

            assertEquals(1, inventoryItems.count())
            assertTrue(inventoryItems.values.single().base is BaseItem.Pickaxe.IcePick)
        }

        @Test
        fun `When registering a new user, the user's market should have plus pickaxe, cross pickaxe, and square pickaxe`() {
            val userId = userUC.registerUser("username", "password")

            val marketItems = transaction(db) { Joins.Market.getItems(EntityID(userId, UserTable)) }

            assertEquals(3, marketItems.count())
            assertTrue(
                marketItems.any { it.value.enum == ItemEnum.PLUS_PICKAXE } &&
                        marketItems.any { it.value.enum == ItemEnum.CROSS_PICKAXE } &&
                        marketItems.any { it.value.enum == ItemEnum.SQUARE_PICKAXE }
            )
        }

        @Test
        fun `When registering a new user, the user's squad should consist of Ice-Cream Wizard, Twolip, and Carpshooter`() {
            val userId = userUC.registerUser("username", "password")

            val units = transaction(db) { SquadTable.getAllies(userId) }

            assertEquals(3, units.count())
            assertTrue(
                units.any { it.enum == MyUnitEnum.ICE_CREAM_WIZARD } &&
                        units.any { it.enum == MyUnitEnum.TWOLIP } &&
                        units.any { it.enum == MyUnitEnum.CARPSHOOTER }
            )
        }
    }
}
