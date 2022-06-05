package com.bitwiserain.pbbg.app.test.domain.usecase

import com.bitwiserain.pbbg.app.BCryptHelper
import com.bitwiserain.pbbg.app.db.repository.UserTable
import com.bitwiserain.pbbg.app.domain.usecase.LoginUC
import com.bitwiserain.pbbg.app.domain.usecase.LoginUCImpl
import com.bitwiserain.pbbg.app.test.db.TestTransaction
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LoginUCImplTest {

    private val userId = 1234
    private val correctUsername = "correctUsername"
    private val correctPassword = "correctPassword"
    private val correctPasswordHash = byteArrayOf(1, 2, 3, 4)

    private val userTable: UserTable = mockk {
        every { getUserByUsername(any()) } returns null
        every { getUserByUsername(correctUsername) } returns mockk {
            every { id } returns userId
            every { passwordHash } returns correctPasswordHash
        }
    }

    private val login: LoginUCImpl = LoginUCImpl(TestTransaction, userTable)

    @BeforeEach
    fun setUp() {
        mockkObject(BCryptHelper)
        with(BCryptHelper) {
            every { verifyPassword(any(), correctPasswordHash) } returns false
            every { verifyPassword(correctPassword, correctPasswordHash) } returns true
        }
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(BCryptHelper)
    }

    @Test
    fun `Given an existing user, when getting the user's ID by correct credentials, the ID should return`() {
        val result = login(correctUsername, correctPassword)

        result.shouldBeTypeOf<LoginUC.Result.Success>()
        result.userId shouldBe userId
    }

    @Test
    fun `Given an existing user, when getting the user's ID by incorrect credentials, no ID should be returned`() {
        val wrongUsername = "wrongUsername"
        val wrongPassword = "wrongPassword"

        /* Test incorrect username */
        login(wrongUsername, correctPassword).shouldBeTypeOf<LoginUC.Result.CredentialsDontMatchError>()

        /* Test incorrect password */
        login(correctUsername, wrongPassword).shouldBeTypeOf<LoginUC.Result.CredentialsDontMatchError>()

        /* Test incorrect username & password */
        login(wrongUsername, wrongPassword).shouldBeTypeOf<LoginUC.Result.CredentialsDontMatchError>()
    }
}
