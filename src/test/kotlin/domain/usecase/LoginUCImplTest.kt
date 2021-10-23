package com.bitwiserain.pbbg.test.domain.usecase

import com.bitwiserain.pbbg.SchemaHelper
import com.bitwiserain.pbbg.domain.usecase.LoginUC
import com.bitwiserain.pbbg.domain.usecase.LoginUCImpl
import com.bitwiserain.pbbg.test.createTestUserAndGetId
import com.bitwiserain.pbbg.test.initDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoginUCImplTest {

    private val db = initDatabase()
    private val login = LoginUCImpl(db)

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(db)
    }

    @Test
    fun `Given an existing user, when getting the user's ID by correct credentials, the ID should return`() {
        val expectedUserId = createTestUserAndGetId(db, username = "username24", password = "pass123")

        val result = login("username24", "pass123")

        assertTrue(result is LoginUC.Result.Success)
        assertEquals(expectedUserId, result.userId)
    }

    @Test
    fun `Given an existing user, when getting the user's ID by incorrect credentials, no ID should be returned`() {
        val expectedUserId = createTestUserAndGetId(db, username = "username24", password = "pass123")

        /* Test incorrect username */
        assertEquals(LoginUC.Result.CredentialsDontMatchError, login("incorrecto17", "pass123"))

        /* Test incorrect password */
        assertEquals(LoginUC.Result.CredentialsDontMatchError, login("username24", "pass12345"))

        /* Test incorrect username & password */
        assertEquals(LoginUC.Result.CredentialsDontMatchError, login("incorrecto17", "pass12345"))
    }
}
