package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.BCryptHelper
import com.bitwiserain.pbbg.PASSWORD_REGEX
import com.bitwiserain.pbbg.SchemaHelper
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.usecase.ChangePasswordUC.Result
import com.bitwiserain.pbbg.test.createTestUserAndGetId
import com.bitwiserain.pbbg.test.initDatabase
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

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

        val newPassword = "new27pas".also { it.matches(PASSWORD_REGEX.toRegex()).shouldBeTrue() }

        changePassword(userId, currentPassword = "pass123", newPassword = newPassword, confirmNewPassword = newPassword)
            .shouldBeTypeOf<Result.Success>()

        val changedUser = transaction(db) { UserTable.getUserById(userId) }

        changedUser.shouldNotBeNull()
        changedUser.username shouldBe "user"
        BCryptHelper.verifyPassword(newPassword, changedUser.passwordHash).shouldBeTrue()
    }

    @Test
    fun `Given an existing user, when changing password with an incorrect current password parameter, WrongCurrentPasswordError should be returned`() {
        val originalPassword = "pass123".also { it.matches(PASSWORD_REGEX.toRegex()).shouldBeTrue() }
        val userId = createTestUserAndGetId(db, "usr71", originalPassword)

        val newPassword = "5fkd^s91$-".also { it.matches(PASSWORD_REGEX.toRegex()).shouldBeTrue() }

        changePassword(userId, "inc0rrect4", newPassword, newPassword)
            .shouldBeTypeOf<Result.WrongCurrentPasswordError>()

        val latestUser = transaction(db) { UserTable.getUserById(userId) }

        latestUser.shouldNotBeNull()
        latestUser.username shouldBe "usr71"
        BCryptHelper.verifyPassword(originalPassword, latestUser.passwordHash).shouldBeTrue()
    }

    @Test
    fun `Given an existing user, when changing password with an incorrectly confirmed new password parameter, UnconfirmedNewPasswordError should be returned`() {
        val originalPassword = "pass123".also { it.matches(PASSWORD_REGEX.toRegex()).shouldBeTrue() }
        val userId = createTestUserAndGetId(db, "usr71", originalPassword)

        val newPassword = "5fkd^s91$-".also { it.matches(PASSWORD_REGEX.toRegex()).shouldBeTrue() }
        val confirmNewPassword = "differ3nt".also { it.matches(PASSWORD_REGEX.toRegex()).shouldBeTrue() }

        changePassword(userId, originalPassword, newPassword, confirmNewPassword)
            // New password not matching its confirmation should return UnconfirmedNewPasswordError.
            .shouldBeTypeOf<Result.UnconfirmedNewPasswordError>()

        val latestUser = transaction(db) { UserTable.getUserById(userId) }

        latestUser.shouldNotBeNull()
        latestUser.username shouldBe "usr71"
        BCryptHelper.verifyPassword(originalPassword, latestUser.passwordHash).shouldBeTrue()
    }

    @Test
    fun `Given an existing user, when changing password reusing the old password, NewPasswordNotNewError should be returned`() {
        val originalPassword = "pass123".also { it.matches(PASSWORD_REGEX.toRegex()).shouldBeTrue() }
        val userId = createTestUserAndGetId(db, "usr71", originalPassword)

        changePassword(userId, originalPassword, originalPassword, originalPassword)
            // New password being the same as the old one should return NewPasswordNotNewError.
            .shouldBeTypeOf<Result.NewPasswordNotNewError>()

        val latestUser = transaction(db) { UserTable.getUserById(userId) }

        latestUser.shouldNotBeNull()
        latestUser.username shouldBe "usr71"
        BCryptHelper.verifyPassword(originalPassword, latestUser.passwordHash).shouldBeTrue()
    }

    @Test
    fun `Given an existing user, when changing password using an invalid password, IllegalPasswordError should be returned`() {
        val originalPassword = "pass123".also { it.matches(PASSWORD_REGEX.toRegex()).shouldBeTrue() }
        val userId = createTestUserAndGetId(db, "usr71", originalPassword)

        val newInvalidPassword = "a".also { it.matches(PASSWORD_REGEX.toRegex()).shouldBeFalse() }

        changePassword(userId, originalPassword, newInvalidPassword, newInvalidPassword)
            // New password being invalid should return IllegalPasswordError.
            .shouldBeTypeOf<Result.IllegalPasswordError>()

        val latestUser = transaction(db) { UserTable.getUserById(userId) }

        latestUser.shouldNotBeNull()
        latestUser.username shouldBe "usr71"
        BCryptHelper.verifyPassword(originalPassword, latestUser.passwordHash).shouldBeTrue()
    }
}
