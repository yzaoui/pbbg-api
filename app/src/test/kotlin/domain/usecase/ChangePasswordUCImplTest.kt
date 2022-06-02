package com.bitwiserain.pbbg.app.test.domain.usecase

import com.bitwiserain.pbbg.app.BCryptHelper
import com.bitwiserain.pbbg.app.PASSWORD_REGEX
import com.bitwiserain.pbbg.app.SchemaHelper
import com.bitwiserain.pbbg.app.db.repository.UserTableImpl
import com.bitwiserain.pbbg.app.domain.usecase.ChangePasswordUC.Result
import com.bitwiserain.pbbg.app.domain.usecase.ChangePasswordUCImpl
import com.bitwiserain.pbbg.app.test.createTestUserAndGetId
import com.bitwiserain.pbbg.app.test.initDatabase
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class ChangePasswordUCImplTest {

    private val transaction = initDatabase()
    private val userTable = UserTableImpl()
    private val changePassword = ChangePasswordUCImpl(transaction, userTable)

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(transaction)
    }

    @Test
    fun `Given an existing user, when changing password with correct parameters, password should be changed`() {
        val userId = createTestUserAndGetId(transaction, userTable, "user", "pass123")

        val newPassword = "new27pas".also { it.matches(PASSWORD_REGEX.toRegex()).shouldBeTrue() }

        changePassword(userId, currentPassword = "pass123", newPassword = newPassword, confirmNewPassword = newPassword)
            .shouldBeTypeOf<Result.Success>()

        val changedUser = transaction { userTable.getUserById(userId) }

        changedUser.shouldNotBeNull()
        changedUser.username shouldBe "user"
        BCryptHelper.verifyPassword(newPassword, changedUser.passwordHash).shouldBeTrue()
    }

    @Test
    fun `Given an existing user, when changing password with an incorrect current password parameter, WrongCurrentPasswordError should be returned`() {
        val originalPassword = "pass123".also { it.matches(PASSWORD_REGEX.toRegex()).shouldBeTrue() }
        val userId = createTestUserAndGetId(transaction, userTable, "usr71", originalPassword)

        val newPassword = "5fkd^s91$-".also { it.matches(PASSWORD_REGEX.toRegex()).shouldBeTrue() }

        changePassword(userId, "inc0rrect4", newPassword, newPassword)
            .shouldBeTypeOf<Result.WrongCurrentPasswordError>()

        val latestUser = transaction { userTable.getUserById(userId) }

        latestUser.shouldNotBeNull()
        latestUser.username shouldBe "usr71"
        BCryptHelper.verifyPassword(originalPassword, latestUser.passwordHash).shouldBeTrue()
    }

    @Test
    fun `Given an existing user, when changing password with an incorrectly confirmed new password parameter, UnconfirmedNewPasswordError should be returned`() {
        val originalPassword = "pass123".also { it.matches(PASSWORD_REGEX.toRegex()).shouldBeTrue() }
        val userId = createTestUserAndGetId(transaction, userTable, "usr71", originalPassword)

        val newPassword = "5fkd^s91$-".also { it.matches(PASSWORD_REGEX.toRegex()).shouldBeTrue() }
        val confirmNewPassword = "differ3nt".also { it.matches(PASSWORD_REGEX.toRegex()).shouldBeTrue() }

        changePassword(userId, originalPassword, newPassword, confirmNewPassword)
            // New password not matching its confirmation should return UnconfirmedNewPasswordError.
            .shouldBeTypeOf<Result.UnconfirmedNewPasswordError>()

        val latestUser = transaction { userTable.getUserById(userId) }

        latestUser.shouldNotBeNull()
        latestUser.username shouldBe "usr71"
        BCryptHelper.verifyPassword(originalPassword, latestUser.passwordHash).shouldBeTrue()
    }

    @Test
    fun `Given an existing user, when changing password reusing the old password, NewPasswordNotNewError should be returned`() {
        val originalPassword = "pass123".also { it.matches(PASSWORD_REGEX.toRegex()).shouldBeTrue() }
        val userId = createTestUserAndGetId(transaction, userTable, "usr71", originalPassword)

        changePassword(userId, originalPassword, originalPassword, originalPassword)
            // New password being the same as the old one should return NewPasswordNotNewError.
            .shouldBeTypeOf<Result.NewPasswordNotNewError>()

        val latestUser = transaction { userTable.getUserById(userId) }

        latestUser.shouldNotBeNull()
        latestUser.username shouldBe "usr71"
        BCryptHelper.verifyPassword(originalPassword, latestUser.passwordHash).shouldBeTrue()
    }

    @Test
    fun `Given an existing user, when changing password using an invalid password, IllegalPasswordError should be returned`() {
        val originalPassword = "pass123".also { it.matches(PASSWORD_REGEX.toRegex()).shouldBeTrue() }
        val userId = createTestUserAndGetId(transaction, userTable, "usr71", originalPassword)

        val newInvalidPassword = "a".also { it.matches(PASSWORD_REGEX.toRegex()).shouldBeFalse() }

        changePassword(userId, originalPassword, newInvalidPassword, newInvalidPassword)
            // New password being invalid should return IllegalPasswordError.
            .shouldBeTypeOf<Result.IllegalPasswordError>()

        val latestUser = transaction { userTable.getUserById(userId) }

        latestUser.shouldNotBeNull()
        latestUser.username shouldBe "usr71"
        BCryptHelper.verifyPassword(originalPassword, latestUser.passwordHash).shouldBeTrue()
    }
}
