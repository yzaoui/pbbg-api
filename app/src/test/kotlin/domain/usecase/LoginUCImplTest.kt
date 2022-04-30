package com.bitwiserain.pbbg.app.test.domain.usecase

import com.bitwiserain.pbbg.app.SchemaHelper
import com.bitwiserain.pbbg.app.db.repository.UserTableImpl
import com.bitwiserain.pbbg.app.domain.usecase.LoginUC
import com.bitwiserain.pbbg.app.domain.usecase.LoginUCImpl
import com.bitwiserain.pbbg.app.test.createTestUserAndGetId
import com.bitwiserain.pbbg.app.test.initDatabase
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class LoginUCImplTest {

    private val transaction = initDatabase()
    private val userTable = UserTableImpl()
    private val login = LoginUCImpl(transaction, userTable)

    @AfterEach
    fun dropDatabase() {
        SchemaHelper.dropTables(transaction)
    }

    @Test
    fun `Given an existing user, when getting the user's ID by correct credentials, the ID should return`() {
        val expectedUserId = createTestUserAndGetId(transaction, userTable, username = "username24", password = "pass123")

        val result = login("username24", "pass123")

        result.shouldBeTypeOf<LoginUC.Result.Success>()
        result.userId shouldBe expectedUserId
    }

    @Test
    fun `Given an existing user, when getting the user's ID by incorrect credentials, no ID should be returned`() {
        val expectedUserId = createTestUserAndGetId(transaction, userTable, username = "username24", password = "pass123")

        /* Test incorrect username */
        login("incorrecto17", "pass123").shouldBeTypeOf<LoginUC.Result.CredentialsDontMatchError>()

        /* Test incorrect password */
        login("username24", "pass12345").shouldBeTypeOf<LoginUC.Result.CredentialsDontMatchError>()

        /* Test incorrect username & password */
        login("incorrecto17", "pass12345").shouldBeTypeOf<LoginUC.Result.CredentialsDontMatchError>()
    }
}
