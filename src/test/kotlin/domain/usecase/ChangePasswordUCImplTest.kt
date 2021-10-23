package com.bitwiserain.pbbg.test.domain.usecase

import com.bitwiserain.pbbg.BCryptHelper
import com.bitwiserain.pbbg.PASSWORD_REGEX
import com.bitwiserain.pbbg.SchemaHelper
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.usecase.ChangePasswordUC.Result
import com.bitwiserain.pbbg.domain.usecase.ChangePasswordUCImpl
import com.bitwiserain.pbbg.test.createTestUserAndGetId
import com.bitwiserain.pbbg.test.initDatabase
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ChangePasswordUCImplTest {

    private val db = initDatabase()
    private val changePassword = ChangePasswordUCImpl(db)

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(db)
    }

    @Test
    fun `Given an existing user, when changing password with correct parameters, password should be changed`() {
        val userId = createTestUserAndGetId(db, "user", "pass123")

        val newPassword = "new27pas".also { assertTrue(it.matches(PASSWORD_REGEX.toRegex())) }

        val result = changePassword(userId, currentPassword = "pass123", newPassword = newPassword, confirmNewPassword = newPassword)

        val changedUser = transaction(db) { UserTable.getUserById(userId) }

        assertTrue(result is Result.Success)
        assertNotNull(changedUser)
        assertEquals("user", changedUser.username)
        assertTrue(BCryptHelper.verifyPassword(newPassword, changedUser.passwordHash))
    }

    @Test
    fun `Given an existing user, when changing password with an incorrect current password parameter, WrongCurrentPasswordError should be returned`() {
        val originalPassword = "pass123".also { assertTrue(it.matches(PASSWORD_REGEX.toRegex())) }
        val userId = createTestUserAndGetId(db, "usr71", originalPassword)

        val newPassword = "5fkd^s91$-".also { assertTrue(it.matches(PASSWORD_REGEX.toRegex())) }

        val result = changePassword(userId, "inc0rrect4", newPassword, newPassword)

        val latestUser = transaction(db) { UserTable.getUserById(userId) }

        assertTrue(result is Result.WrongCurrentPasswordError)
        assertNotNull(latestUser)
        assertEquals("usr71", latestUser.username)
        assertTrue(BCryptHelper.verifyPassword(originalPassword, latestUser.passwordHash))
    }

    @Test
    fun `Given an existing user, when changing password with an incorrectly confirmed new password parameter, UnconfirmedNewPasswordError should be returned`() {
        val originalPassword = "pass123".also { assertTrue(it.matches(PASSWORD_REGEX.toRegex())) }
        val userId = createTestUserAndGetId(db, "usr71", originalPassword)

        val newPassword = "5fkd^s91$-".also { assertTrue(it.matches(PASSWORD_REGEX.toRegex())) }
        val confirmNewPassword = "differ3nt".also { assertTrue(it.matches(PASSWORD_REGEX.toRegex())) }

        val result = changePassword(userId, originalPassword, newPassword, confirmNewPassword)

        val latestUser = transaction(db) { UserTable.getUserById(userId) }

        assertTrue(result is Result.UnconfirmedNewPasswordError, "New password not matching its confirmation should return UnconfirmedNewPasswordError.")
        assertNotNull(latestUser)
        assertEquals("usr71", latestUser.username)
        assertTrue(BCryptHelper.verifyPassword(originalPassword, latestUser.passwordHash))
    }

    @Test
    fun `Given an existing user, when changing password reusing the old password, NewPasswordNotNewError should be returned`() {
        val originalPassword = "pass123".also { assertTrue(it.matches(PASSWORD_REGEX.toRegex())) }
        val userId = createTestUserAndGetId(db, "usr71", originalPassword)

        val result = changePassword(userId, originalPassword, originalPassword, originalPassword)

        val latestUser = transaction(db) { UserTable.getUserById(userId) }

        assertTrue(result is Result.NewPasswordNotNewError, "New password being the same as the old one should return NewPasswordNotNewError.")
        assertNotNull(latestUser)
        assertEquals("usr71", latestUser.username)
        assertTrue(BCryptHelper.verifyPassword(originalPassword, latestUser.passwordHash))
    }

    @Test
    fun `Given an existing user, when changing password using an invalid password, IllegalPasswordError should be returned`() {
        val originalPassword = "pass123".also { assertTrue(it.matches(PASSWORD_REGEX.toRegex())) }
        val userId = createTestUserAndGetId(db, "usr71", originalPassword)

        val newInvalidPassword = "a".also { assertFalse(it.matches(PASSWORD_REGEX.toRegex())) }

        val result = changePassword(userId, originalPassword, newInvalidPassword, newInvalidPassword)

        val latestUser = transaction(db) { UserTable.getUserById(userId) }

        assertTrue(result is Result.IllegalPasswordError, "New password being invalid should return IllegalPasswordError.")
        assertNotNull(latestUser)
        assertEquals("usr71", latestUser.username)
        assertTrue(BCryptHelper.verifyPassword(originalPassword, latestUser.passwordHash))
    }
}
