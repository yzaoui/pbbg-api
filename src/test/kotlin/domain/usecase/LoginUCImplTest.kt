package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.SchemaHelper
import com.bitwiserain.pbbg.test.createTestUserAndGetId
import com.bitwiserain.pbbg.test.initDatabase
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

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

        result.shouldBeTypeOf<LoginUC.Result.Success>()
        result.userId shouldBe expectedUserId
    }

    @Test
    fun `Given an existing user, when getting the user's ID by incorrect credentials, no ID should be returned`() {
        val expectedUserId = createTestUserAndGetId(db, username = "username24", password = "pass123")

        /* Test incorrect username */
        login("incorrecto17", "pass123").shouldBeTypeOf<LoginUC.Result.CredentialsDontMatchError>()

        /* Test incorrect password */
        login("username24", "pass12345").shouldBeTypeOf<LoginUC.Result.CredentialsDontMatchError>()

        /* Test incorrect username & password */
        login("incorrecto17", "pass12345").shouldBeTypeOf<LoginUC.Result.CredentialsDontMatchError>()
    }
}
