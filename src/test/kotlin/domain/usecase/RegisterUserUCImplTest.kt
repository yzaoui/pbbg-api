package com.bitwiserain.pbbg.test.domain.usecase

import com.bitwiserain.pbbg.PASSWORD_REGEX
import com.bitwiserain.pbbg.PASSWORD_REGEX_DESCRIPTION
import com.bitwiserain.pbbg.SchemaHelper
import com.bitwiserain.pbbg.USERNAME_REGEX
import com.bitwiserain.pbbg.USERNAME_REGEX_DESCRIPTION
import com.bitwiserain.pbbg.db.repository.Joins
import com.bitwiserain.pbbg.db.repository.SquadTable
import com.bitwiserain.pbbg.db.repository.UserStatsTable
import com.bitwiserain.pbbg.domain.model.ItemEnum
import com.bitwiserain.pbbg.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.domain.usecase.RegisterUserUC.Result
import com.bitwiserain.pbbg.domain.usecase.RegisterUserUCImpl
import com.bitwiserain.pbbg.test.MutableClock
import com.bitwiserain.pbbg.test.createTestUserAndGetId
import com.bitwiserain.pbbg.test.initDatabase
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RegisterUserUCImplTest {

    private val db = initDatabase()
    private val clock = MutableClock()
    private val registerUser = RegisterUserUCImpl(db, clock)

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(db)
    }

    @Nested
    inner class SuccessfulRegistration {
        @Test
        fun `When registering a new user, the user should have 0 gold, 0 mining exp, and 0 farming exp`() {
            val userId = (registerUser("username", "password") as Result.Success).userId

            val stats = transaction(db) { UserStatsTable.getUserStats(userId) }

            assertEquals(0, stats.gold)
            assertEquals(0, stats.miningExp)
            assertEquals(0, stats.farmingExp)
        }

        @Test
        fun `When registering a new user, the user's inventory should contain 1 ice pick, 2 apple saplings, 5 tomato seeds`() {
            val userId = (registerUser("username", "password") as Result.Success).userId

            val inventoryItems = transaction(db) { Joins.getInventoryItems(userId) }

            assertEquals(3, inventoryItems.count())
            // TODO: Finish this test
        }

        @Test
        fun `When registering a new user, the user's market should have plus pickaxe, cross pickaxe, and square pickaxe`() {
            val userId = (registerUser("username", "password") as Result.Success).userId

            val marketItems = transaction(db) { Joins.Market.getItems(userId) }

            assertEquals(3, marketItems.count())
            assertTrue(
                marketItems.any { it.value.enum == ItemEnum.PLUS_PICKAXE } &&
                        marketItems.any { it.value.enum == ItemEnum.CROSS_PICKAXE } &&
                        marketItems.any { it.value.enum == ItemEnum.SQUARE_PICKAXE }
            )
        }

        @Test
        fun `When registering a new user, the user's squad should consist of Ice-Cream Wizard, Twolip, and Carpshooter`() {
            val userId = (registerUser("username", "password") as Result.Success).userId

            val units = transaction(db) { SquadTable.getAllies(userId) }

            assertEquals(3, units.count())
            assertTrue(
                units.any { it.enum == MyUnitEnum.ICE_CREAM_WIZARD } &&
                        units.any { it.enum == MyUnitEnum.TWOLIP } &&
                        units.any { it.enum == MyUnitEnum.CARPSHOOTER }
            )
        }
    }

    @Nested
    inner class FailedRegistration {
        @Test
        fun `Given an existing user, when registering a new user with the same username, UsernameNotAvailableError should be returned`() {
            createTestUserAndGetId(db, username = "username")

            assertTrue(registerUser("username", "password") is Result.UsernameNotAvailableError)
        }

        @Test
        fun `When registering a new user with invalid credentials, CredentialsFormatError should be returned with appropriate members`() {
            val invalidUsername = "usdlkfjsglksdjlkflksdjfmlkdsj"
            val validUsername = "username"
            val invalidPassword = "p"
            val validPassword = "password"

            /* Make sure test isn't wrong */
            assertFalse(invalidUsername.matches(USERNAME_REGEX.toRegex()))
            assertFalse(invalidPassword.matches(PASSWORD_REGEX.toRegex()))
            assertTrue(validUsername.matches(USERNAME_REGEX.toRegex()))
            assertTrue(validPassword.matches(USERNAME_REGEX.toRegex()))

            /* Test invalid username, valid password */
            val result1 = registerUser(invalidUsername, validPassword)
            assertTrue(result1 is Result.CredentialsFormatError)
            assertEquals(USERNAME_REGEX_DESCRIPTION, result1.usernameError)
            assertNull(result1.passwordError)

            /* Test valid username, invalid password */
            val result2 = registerUser(validUsername, invalidPassword)
            assertTrue(result2 is Result.CredentialsFormatError)
            assertNull(result2.usernameError)
            assertEquals(PASSWORD_REGEX_DESCRIPTION, result2.passwordError)

            /* Test invalid username, invalid password */
            val result3 = registerUser(invalidUsername, invalidPassword)
            assertTrue(result3 is Result.CredentialsFormatError)
            assertEquals(USERNAME_REGEX_DESCRIPTION, result3.usernameError)
            assertEquals(PASSWORD_REGEX_DESCRIPTION, result3.passwordError)
        }
    }
}
