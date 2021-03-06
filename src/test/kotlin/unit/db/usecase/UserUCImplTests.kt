package com.bitwiserain.pbbg.test.unit.db.usecase

import com.bitwiserain.pbbg.*
import com.bitwiserain.pbbg.db.repository.Joins
import com.bitwiserain.pbbg.db.repository.SquadTable
import com.bitwiserain.pbbg.db.repository.UserStatsTable
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.db.usecase.UserUCImpl
import com.bitwiserain.pbbg.domain.model.ItemEnum
import com.bitwiserain.pbbg.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.domain.usecase.*
import com.bitwiserain.pbbg.test.MutableClock
import com.bitwiserain.pbbg.test.createTestUserAndGetId
import com.bitwiserain.pbbg.test.initDatabase
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import kotlin.test.*

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class UserUCImplTests {
    private val db = initDatabase()
    private val clock = MutableClock()
    private val userUC: UserUC = UserUCImpl(db, clock)

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(db)
    }

    @Nested
    inner class SuccessfulRegistration {
        @Test
        fun `When registering a new user, the user should have 0 gold, 0 mining exp, and 0 farming exp`() {
            val userId = userUC.registerUser("username", "password")

            val stats = transaction(db) { UserStatsTable.getUserStats(userId) }

            assertEquals(0, stats.gold)
            assertEquals(0, stats.miningExp)
            assertEquals(0, stats.farmingExp)
        }

        @Test
        fun `When registering a new user, the user's inventory should contain 1 ice pick, 2 apple saplings, 5 tomato seeds`() {
            val userId = userUC.registerUser("username", "password")

            val inventoryItems = transaction(db) { Joins.getInventoryItems(userId) }

            assertEquals(3, inventoryItems.count())
            // TODO: Finish this test
        }

        @Test
        fun `When registering a new user, the user's market should have plus pickaxe, cross pickaxe, and square pickaxe`() {
            val userId = userUC.registerUser("username", "password")

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

    @Nested
    inner class FailedRegistration {
        @Test
        fun `Given an existing user, when registering a new user with the same username, UsernameNotAvailableException should be thrown`() {
            createTestUserAndGetId(db, username = "username")

            assertThrows<UsernameNotAvailableException> {
                userUC.registerUser("username", "password")
            }
        }

        @Test
        fun `When registering a new user with invalid credentials, CredentialsFormatException should be thrown with appropriate members`() {
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
            assertThrows<CredentialsFormatException> {
                userUC.registerUser(invalidUsername, validPassword)
            }.also { e ->
                assertEquals(USERNAME_REGEX_DESCRIPTION, e.usernameError)
                assertNull(e.passwordError)
            }

            /* Test valid username, invalid password */
            assertThrows<CredentialsFormatException> {
                userUC.registerUser(validUsername, invalidPassword)
            }.also { e ->
                assertNull(e.usernameError)
                assertEquals(PASSWORD_REGEX_DESCRIPTION, e.passwordError)
            }

            /* Test invalid username, invalid password */
            assertThrows<CredentialsFormatException> {
                userUC.registerUser(invalidUsername, invalidPassword)
            }.also { e ->
                assertEquals(USERNAME_REGEX_DESCRIPTION, e.usernameError)
                assertEquals(PASSWORD_REGEX_DESCRIPTION, e.passwordError)
            }
        }
    }

    @Nested
    inner class ByCredentials {
        @Test
        fun `Given an existing user, when getting the user's ID by correct credentials, the ID should return`() {
            val expectedUserId = createTestUserAndGetId(db, username = "username24", password = "pass123")

            val actualUserId = userUC.getUserIdByCredentials("username24", "pass123")

            assertEquals(expectedUserId, actualUserId)
        }

        @Test
        fun `Given an existing user, when getting the user's ID by incorrect credentials, no ID should be returned`() {
            val expectedUserId = createTestUserAndGetId(db, username = "username24", password = "pass123")

            /* Test incorrect username */
            assertNull(userUC.getUserIdByCredentials("incorrecto17", "pass123"))

            /* Test incorrect password */
            assertNull(userUC.getUserIdByCredentials("username24", "pass12345"))

            /* Test incorrect username & password */
            assertNull(userUC.getUserIdByCredentials("incorrecto17", "pass12345"))
        }
    }

    @Nested
    inner class UserStats {
        @Test
        fun `When getting user stats by ID, the user stats should return`() {
            val userId = createTestUserAndGetId(db)

            transaction(db) {
                UserStatsTable.createUserStats(userId)
                UserStatsTable.updateGold(userId, 20)
                UserStatsTable.updateMiningExp(userId, 500)
            }

            val stats = userUC.getUserStatsByUserId(userId)

            assertEquals(20, stats.gold)
            assertEquals(500, stats.miningExp)
        }
    }

    @Nested
    inner class ChangePassword {
        @Test
        fun `Given an existing user, when changing password with correct parameters, password should be changed`() {
            val userId = createTestUserAndGetId(db, "user", "pass123")

            val newPassword = "new27pas".also { assertTrue(it.matches(PASSWORD_REGEX.toRegex())) }

            userUC.changePassword(userId, currentPassword = "pass123", newPassword = newPassword, confirmNewPassword = newPassword)

            val changedUser = transaction(db) { UserTable.getUserById(userId) }

            assertNotNull(changedUser)
            assertEquals("user", changedUser.username)
            assertTrue(BCryptHelper.verifyPassword(newPassword, changedUser.passwordHash))
        }

        @Test
        fun `Given an existing user, when changing password with an incorrect current password parameter, WrongCurrentPasswordException should be thrown`() {
            val originalPassword = "pass123".also { assertTrue(it.matches(PASSWORD_REGEX.toRegex())) }
            val userId = createTestUserAndGetId(db, "usr71", originalPassword)

            val newPassword = "5fkd^s91$-".also { assertTrue(it.matches(PASSWORD_REGEX.toRegex())) }

            assertThrows<WrongCurrentPasswordException> {
                userUC.changePassword(userId, "inc0rrect4", newPassword, newPassword)
            }

            val latestUser = transaction(db) { UserTable.getUserById(userId) }

            assertNotNull(latestUser)
            assertEquals("usr71", latestUser.username)
            assertTrue(BCryptHelper.verifyPassword(originalPassword, latestUser.passwordHash))
        }

        @Test
        fun `Given an existing user, when changing password with an incorrectly confirmed new password parameter, UnconfirmedNewPasswordException should be thrown`() {
            val originalPassword = "pass123".also { assertTrue(it.matches(PASSWORD_REGEX.toRegex())) }
            val userId = createTestUserAndGetId(db, "usr71", originalPassword)

            val newPassword = "5fkd^s91$-".also { assertTrue(it.matches(PASSWORD_REGEX.toRegex())) }
            val confirmNewPassword = "differ3nt".also { assertTrue(it.matches(PASSWORD_REGEX.toRegex())) }

            assertThrows<UnconfirmedNewPasswordException>("New password not matching its confirmation should throw UnconfirmedNewPasswordException.") {
                userUC.changePassword(userId, originalPassword, newPassword, confirmNewPassword)
            }

            val latestUser = transaction(db) { UserTable.getUserById(userId) }

            assertNotNull(latestUser)
            assertEquals("usr71", latestUser.username)
            assertTrue(BCryptHelper.verifyPassword(originalPassword, latestUser.passwordHash))
        }

        @Test
        fun `Given an existing user, when changing password reusing the old password, NewPasswordNotNewException should be thrown`() {
            val originalPassword = "pass123".also { assertTrue(it.matches(PASSWORD_REGEX.toRegex())) }
            val userId = createTestUserAndGetId(db, "usr71", originalPassword)

            assertThrows<NewPasswordNotNewException>("New password being the same as the old one should throw UnconfirmedNewPasswordException.") {
                userUC.changePassword(userId, originalPassword, originalPassword, originalPassword)
            }

            val latestUser = transaction(db) { UserTable.getUserById(userId) }

            assertNotNull(latestUser)
            assertEquals("usr71", latestUser.username)
            assertTrue(BCryptHelper.verifyPassword(originalPassword, latestUser.passwordHash))
        }

        @Test
        fun `Given an existing user, when changing password using an invalid password, IllegalPasswordException should be thrown`() {
            val originalPassword = "pass123".also { assertTrue(it.matches(PASSWORD_REGEX.toRegex())) }
            val userId = createTestUserAndGetId(db, "usr71", originalPassword)

            val newInvalidPassword = "a".also { assertFalse(it.matches(PASSWORD_REGEX.toRegex())) }

            assertThrows<IllegalPasswordException>("New password being invalid should throw IllegalPasswordException.") {
                userUC.changePassword(userId, originalPassword, newInvalidPassword, newInvalidPassword)
            }

            val latestUser = transaction(db) { UserTable.getUserById(userId) }

            assertNotNull(latestUser)
            assertEquals("usr71", latestUser.username)
            assertTrue(BCryptHelper.verifyPassword(originalPassword, latestUser.passwordHash))
        }
    }
}
