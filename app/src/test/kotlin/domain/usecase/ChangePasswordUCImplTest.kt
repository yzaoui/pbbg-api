package com.bitwiserain.pbbg.app.test.domain.usecase

import com.bitwiserain.pbbg.app.BCryptHelper
import com.bitwiserain.pbbg.app.PASSWORD_REGEX
import com.bitwiserain.pbbg.app.db.model.User
import com.bitwiserain.pbbg.app.db.repository.UserTable
import com.bitwiserain.pbbg.app.domain.usecase.ChangePasswordUC.Result
import com.bitwiserain.pbbg.app.domain.usecase.ChangePasswordUCImpl
import com.bitwiserain.pbbg.app.test.db.TestTransaction
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class ChangePasswordUCImplTest {

    private val userId = 1234

    private val userTable: UserTable = mockk(relaxUnitFun = true)

    private val changePassword: ChangePasswordUCImpl = ChangePasswordUCImpl(TestTransaction, userTable)

    @BeforeEach
    fun setUp() {
        mockkObject(BCryptHelper)
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(BCryptHelper)
    }

    @Test
    fun `Given an existing user, when changing password with correct parameters, password should be changed`() {
        val currentPassword = "currentPassword"
        val currentPasswordHash = byteArrayOf(1, 2, 3, 4)
        val newPassword = "newPassword"
        val newPasswordHash = byteArrayOf(5, 6, 7, 8)
        val confirmNewPassword = "newPassword"
        assert(newPassword == confirmNewPassword )
        assert(newPassword.matches(PASSWORD_REGEX.toRegex()))

        every { userTable.getUserById(userId) } returns User(
            id = userId,
            username = "user",
            passwordHash = currentPasswordHash,
            joinedInstant = Instant.now()
        )
        every { BCryptHelper.verifyPassword(currentPassword, currentPasswordHash) } returns true
        every { BCryptHelper.hashPassword(newPassword) } returns newPasswordHash


        val result = changePassword(userId, currentPassword, newPassword, confirmNewPassword)

        verify { userTable.updatePassword(userId, newPasswordHash) }

        result shouldBe Result.Success
    }

    @Test
    fun `Given an existing user, when changing password with an incorrect current password parameter, WrongCurrentPasswordError should be returned`() {
        val actualCurrentPasswordHash = byteArrayOf(1, 2, 3, 4)
        val attemptedCurrentPassword = "inc0rrect4"
        val newPassword = "newPassword"

        every { userTable.getUserById(userId) } returns User(
            id = userId,
            username = "user",
            passwordHash = actualCurrentPasswordHash,
            joinedInstant = Instant.now()
        )
        // Current password fails to verify
        every { BCryptHelper.verifyPassword(attemptedCurrentPassword, actualCurrentPasswordHash) } returns false

        changePassword(userId, attemptedCurrentPassword, newPassword, newPassword)
            .shouldBeTypeOf<Result.WrongCurrentPasswordError>()
    }

    @Test
    fun `Given an existing user, when changing password with an incorrectly confirmed new password parameter, UnconfirmedNewPasswordError should be returned`() {
        val currentPassword = "currentPassword"
        val currentPasswordHash = byteArrayOf(1, 2, 3, 4)
        val newPassword = "newPassword"
        val confirmNewPassword = "newPasswordDifferent"
        assert(newPassword != confirmNewPassword )

        every { userTable.getUserById(userId) } returns User(
            id = userId,
            username = "user",
            passwordHash = currentPasswordHash,
            joinedInstant = Instant.now()
        )
        every { BCryptHelper.verifyPassword(currentPassword, currentPasswordHash) } returns true

        changePassword(userId, currentPassword, newPassword, confirmNewPassword)
            // New password not matching its confirmation should return UnconfirmedNewPasswordError.
            .shouldBeTypeOf<Result.UnconfirmedNewPasswordError>()
    }

    @Test
    fun `Given an existing user, when changing password reusing the old password, NewPasswordNotNewError should be returned`() {
        val currentPassword = "currentPassword"
        val currentPasswordHash = byteArrayOf(1, 2, 3, 4)

        every { userTable.getUserById(userId) } returns User(
            id = userId,
            username = "user",
            passwordHash = currentPasswordHash,
            joinedInstant = Instant.now()
        )
        every { BCryptHelper.verifyPassword(currentPassword, currentPasswordHash) } returns true

        changePassword(userId, currentPassword, currentPassword, currentPassword)
            // New password being the same as the old one should return NewPasswordNotNewError.
            .shouldBeTypeOf<Result.NewPasswordNotNewError>()
    }

    @Test
    fun `Given an existing user, when changing password using an invalid password, IllegalPasswordError should be returned`() {
        val currentPassword = "currentPassword"
        val currentPasswordHash = byteArrayOf(1, 2, 3, 4)
        val newPassword = "a"
        // Invalid password
        assert(!newPassword.matches(PASSWORD_REGEX.toRegex()))

        every { userTable.getUserById(userId) } returns User(
            id = userId,
            username = "user",
            passwordHash = currentPasswordHash,
            joinedInstant = Instant.now()
        )
        every { BCryptHelper.verifyPassword(currentPassword, currentPasswordHash) } returns true

        changePassword(userId, currentPassword, newPassword, newPassword)
            // New password being invalid should return IllegalPasswordError.
            .shouldBeTypeOf<Result.IllegalPasswordError>()
    }
}
