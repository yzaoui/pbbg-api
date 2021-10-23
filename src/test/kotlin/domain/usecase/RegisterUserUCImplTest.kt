package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.PASSWORD_REGEX
import com.bitwiserain.pbbg.PASSWORD_REGEX_DESCRIPTION
import com.bitwiserain.pbbg.SchemaHelper
import com.bitwiserain.pbbg.USERNAME_REGEX
import com.bitwiserain.pbbg.USERNAME_REGEX_DESCRIPTION
import com.bitwiserain.pbbg.db.repository.DexTableImpl
import com.bitwiserain.pbbg.db.repository.ItemHistoryTableImpl
import com.bitwiserain.pbbg.db.repository.Joins
import com.bitwiserain.pbbg.db.repository.MaterializedItemTableImpl
import com.bitwiserain.pbbg.db.repository.SquadTableImpl
import com.bitwiserain.pbbg.db.repository.UserStatsTable
import com.bitwiserain.pbbg.db.repository.farm.PlotTableImpl
import com.bitwiserain.pbbg.db.repository.market.MarketInventoryTableImpl
import com.bitwiserain.pbbg.db.repository.market.MarketTableImpl
import com.bitwiserain.pbbg.domain.model.ItemEnum
import com.bitwiserain.pbbg.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.domain.usecase.RegisterUserUC.Result
import com.bitwiserain.pbbg.test.MutableClock
import com.bitwiserain.pbbg.test.createTestUserAndGetId
import com.bitwiserain.pbbg.test.initDatabase
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RegisterUserUCImplTest {

    private val db = initDatabase()
    private val clock = MutableClock()
    private val dexTable = DexTableImpl()
    private val itemHistoryTable = ItemHistoryTableImpl()
    private val marketTable = MarketTableImpl()
    private val marketInventoryTable = MarketInventoryTableImpl()
    private val materializedItemTable = MaterializedItemTableImpl()
    private val plotTable = PlotTableImpl()
    private val squadTable = SquadTableImpl()
    private val registerUser = RegisterUserUCImpl(db, clock, dexTable, itemHistoryTable, marketTable, marketInventoryTable, materializedItemTable, plotTable, squadTable)

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

            assertSoftly(stats) {
                gold shouldBe 0
                miningExp shouldBe 0
                farmingExp shouldBe 0
            }
        }

        @Test
        fun `When registering a new user, the user's inventory should contain 1 ice pick, 2 apple saplings, 5 tomato seeds`() {
            val userId = (registerUser("username", "password") as Result.Success).userId

            val inventoryItems = transaction(db) { Joins.getInventoryItems(userId) }

            inventoryItems shouldHaveSize 3
            // TODO: Finish this test
        }

        @Test
        fun `When registering a new user, the user's market should have plus pickaxe, cross pickaxe, and square pickaxe`() {
            val userId = (registerUser("username", "password") as Result.Success).userId

            val marketItems = transaction(db) { Joins.Market.getItems(userId) }

            marketItems shouldHaveSize 3
            marketItems.values.shouldExist { it.enum == ItemEnum.PLUS_PICKAXE }
            marketItems.values.shouldExist { it.enum == ItemEnum.CROSS_PICKAXE }
            marketItems.values.shouldExist { it.enum == ItemEnum.SQUARE_PICKAXE }
        }

        @Test
        fun `When registering a new user, the user's squad should consist of Ice-Cream Wizard, Twolip, and Carpshooter`() {
            val userId = (registerUser("username", "password") as Result.Success).userId

            val units = transaction(db) { squadTable.getAllies(userId) }

            assertSoftly(units) {
                shouldHaveSize(3)
                shouldExist { it.enum == MyUnitEnum.ICE_CREAM_WIZARD }
                shouldExist { it.enum == MyUnitEnum.TWOLIP }
                shouldExist { it.enum == MyUnitEnum.CARPSHOOTER }
            }
        }
    }

    @Nested
    inner class FailedRegistration {
        @Test
        fun `Given an existing user, when registering a new user with the same username, UsernameNotAvailableError should be returned`() {
            createTestUserAndGetId(db, username = "username")

            registerUser("username", "password").shouldBeTypeOf<Result.UsernameNotAvailableError>()
        }

        @Test
        fun `When registering a new user with invalid credentials, CredentialsFormatError should be returned with appropriate members`() {
            val invalidUsername = "usdlkfjsglksdjlkflksdjfmlkdsj"
            val validUsername = "username"
            val invalidPassword = "p"
            val validPassword = "password"

            /* Make sure test isn't wrong */
            assert(!invalidUsername.matches(USERNAME_REGEX.toRegex()))
            assert(!invalidPassword.matches(PASSWORD_REGEX.toRegex()))
            assert(validUsername.matches(USERNAME_REGEX.toRegex()))
            assert(validPassword.matches(USERNAME_REGEX.toRegex()))

            /* Test invalid username, valid password */
            assertSoftly(registerUser(invalidUsername, validPassword)) {
                shouldBeTypeOf<Result.CredentialsFormatError>()
                usernameError shouldBe USERNAME_REGEX_DESCRIPTION
                passwordError.shouldBeNull()
            }

            /* Test valid username, invalid password */
            assertSoftly(registerUser(validUsername, invalidPassword)) {
                shouldBeTypeOf<Result.CredentialsFormatError>()
                usernameError.shouldBeNull()
                passwordError shouldBe PASSWORD_REGEX_DESCRIPTION
            }


            /* Test invalid username, invalid password */
            assertSoftly(registerUser(invalidUsername, invalidPassword)) {
                shouldBeTypeOf<Result.CredentialsFormatError>()
                usernameError shouldBe USERNAME_REGEX_DESCRIPTION
                passwordError shouldBe PASSWORD_REGEX_DESCRIPTION
            }
        }
    }
}
