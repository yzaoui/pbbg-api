package com.bitwiserain.pbbg.test.db.usecase

import com.bitwiserain.pbbg.db.repository.DexTable
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.db.usecase.DexUCImpl
import com.bitwiserain.pbbg.domain.model.ItemEnum
import com.bitwiserain.pbbg.domain.usecase.DexUC
import com.bitwiserain.pbbg.test.initDatabase
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class DexUCImplTests {
    private val db = initDatabase()
    private val dexUC: DexUC = DexUCImpl(db)

    @AfterEach
    fun dropDatabase() {
        com.bitwiserain.pbbg.test.dropDatabase(db)
    }

    @Nested
    inner class DexItems {
        @Test
        fun `Given a user who's discovered some items, when calling for dex items, those exact ones should return`() {
            val userId = createTestUserAndGetId(db)

            val discoveredItemEnums = setOf(ItemEnum.COPPER_ORE, ItemEnum.ICE_PICK)

            transaction(db) {
                DexTable.insertDiscovered(userId, discoveredItemEnums)
            }

            val dexItems = dexUC.getDexItems(userId.value)

            assertEquals(dexItems.discoveredItems, discoveredItemEnums)
        }

        @Test
        fun `Given a user who hasn't discovered the last item, when calling for dex items, lastItemIsDiscovered should be false`() {
            val userId = createTestUserAndGetId(db)

            val discoveredItemEnums = setOf(ItemEnum.COPPER_ORE, ItemEnum.ICE_PICK).minus(ItemEnum.values().last())

            transaction(db) {
                DexTable.insertDiscovered(userId, discoveredItemEnums)
            }

            val dexItems = dexUC.getDexItems(userId.value)

            assertFalse(dexItems.lastItemIsDiscovered)
        }

        @Test
        fun `Given a user who has discovered the last item, when calling for dex items, lastItemIsDiscovered should be true`() {
            val userId = createTestUserAndGetId(db)

            val discoveredItemEnums = setOf(ItemEnum.COPPER_ORE, ItemEnum.ICE_PICK).plus(ItemEnum.values().last())

            transaction(db) {
                DexTable.insertDiscovered(userId, discoveredItemEnums)
            }

            val dexItems = dexUC.getDexItems(userId.value)

            assertTrue(dexItems.lastItemIsDiscovered)
        }
    }

    private fun createTestUserAndGetId(db: Database): EntityID<Int> = transaction(db) {
        UserTable.createUserAndGetId("testuser", ByteArray(60))
    }
}
