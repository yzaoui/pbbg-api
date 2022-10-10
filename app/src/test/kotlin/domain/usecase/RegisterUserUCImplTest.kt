package com.bitwiserain.pbbg.app.test.domain.usecase

import com.bitwiserain.pbbg.app.BCryptHelper
import com.bitwiserain.pbbg.app.PASSWORD_REGEX
import com.bitwiserain.pbbg.app.PASSWORD_REGEX_DESCRIPTION
import com.bitwiserain.pbbg.app.USERNAME_REGEX
import com.bitwiserain.pbbg.app.USERNAME_REGEX_DESCRIPTION
import com.bitwiserain.pbbg.app.db.repository.DexTable
import com.bitwiserain.pbbg.app.db.repository.InventoryTable
import com.bitwiserain.pbbg.app.db.repository.ItemHistoryTable
import com.bitwiserain.pbbg.app.db.repository.MaterializedItemTable
import com.bitwiserain.pbbg.app.db.repository.SquadTable
import com.bitwiserain.pbbg.app.db.repository.UnitTable
import com.bitwiserain.pbbg.app.db.repository.UserStatsTable
import com.bitwiserain.pbbg.app.db.repository.UserTable
import com.bitwiserain.pbbg.app.db.repository.farm.PlotListTable
import com.bitwiserain.pbbg.app.db.repository.farm.PlotTable
import com.bitwiserain.pbbg.app.db.repository.market.MarketInventoryTable
import com.bitwiserain.pbbg.app.db.repository.market.MarketTable
import com.bitwiserain.pbbg.app.domain.usecase.RegisterUserUC.Result
import com.bitwiserain.pbbg.app.domain.usecase.RegisterUserUCImpl
import com.bitwiserain.pbbg.app.test.db.TestTransaction
import com.bitwiserain.pbbg.app.test.db.repository.MarketTableTestImpl
import com.bitwiserain.pbbg.app.test.db.repository.MaterializedItemTableTestImpl
import com.bitwiserain.pbbg.app.test.db.repository.UnitTableTestImpl
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verifyAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Clock

class RegisterUserUCImplTest {

    private val userId = 1234
    private val existingUser = "existingUser"
    private val validUsername = "username".also { assert(it.matches(USERNAME_REGEX.toRegex())) }
    private val validPassword = "password".also { assert(it.matches(PASSWORD_REGEX.toRegex())) }
    private val invalidUsername = "usdlkfjsglksdjlkflksdjfmlkdsj".also { assert(!it.matches(USERNAME_REGEX.toRegex())) }
    private val invalidPassword = "p".also { assert(!it.matches(PASSWORD_REGEX.toRegex())) }
    private val validPasswordHash = byteArrayOf(0, 1, 2, 3)

    private val marketIds: MutableMap<Int, Int> = mutableMapOf()

    private val clock = Clock.systemUTC()
    private val dexTable: DexTable = mockk(relaxUnitFun = true)
    private val inventoryTable: InventoryTable = mockk(relaxUnitFun = true)
    private val itemHistoryTable: ItemHistoryTable = mockk(relaxUnitFun = true)
    private val marketTable: MarketTable = MarketTableTestImpl(marketIds)
    private val marketInventoryTable: MarketInventoryTable = mockk(relaxUnitFun = true)
    private val materializedItemTable: MaterializedItemTable = MaterializedItemTableTestImpl()
    private val plotTable: PlotTable = mockk {
        every { createAndGetEmptyPlot(userId) } returns mockk()
    }
    private val plotListTable: PlotListTable = mockk(relaxUnitFun = true)
    private val squadTable: SquadTable = mockk(relaxUnitFun = true)
    private val unitTable: UnitTable = UnitTableTestImpl()
    private val userTable: UserTable = mockk {
        every { getUserByUsername(any()) } returns null
        every { getUserByUsername(existingUser) } returns mockk()
        every { createUserAndGetId(validUsername, validPasswordHash, any()) } returns userId
    }
    private val userStatsTable: UserStatsTable = mockk(relaxUnitFun = true)

    private val registerUser: RegisterUserUCImpl = RegisterUserUCImpl(
        TestTransaction, clock, dexTable, inventoryTable, itemHistoryTable, marketTable, marketInventoryTable, materializedItemTable, plotTable, plotListTable, squadTable,
        unitTable, userTable, userStatsTable
    )

    @BeforeEach
    fun setUp() {
        mockkObject(BCryptHelper)
        with(BCryptHelper) {
            every { hashPassword(validPassword) } returns validPasswordHash
        }
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(BCryptHelper)
    }

    // TODO: Update these test names
    @Nested
    inner class SuccessfulRegistration {
        @Test
        fun `When registering a new user, the user should have 0 gold, 0 mining exp, and 0 farming exp`() {
            registerUser(validUsername, validPassword) shouldBe Result.Success(userId)

            // Assuming that this query sets those attributes to 0
            verifyAll { userStatsTable.createUserStats(userId) }
        }

        @Test
        @Disabled
        fun `When registering a new user, the user's inventory should contain 1 ice pick, 2 apple saplings, 5 tomato seeds`() {
            registerUser(validUsername, validPassword) shouldBe Result.Success(userId)

            // TODO: Finish
        }

        @Test
        @Disabled
        fun `When registering a new user, the user's market should have plus pickaxe, cross pickaxe, and square pickaxe`() {
            registerUser(validUsername, validPassword) shouldBe Result.Success(userId)

            // TODO: Finish
        }

        @Test
        @Disabled
        fun `When registering a new user, the user's squad should consist of Ice-Cream Wizard, Twolip, and Carpshooter`() {
            registerUser(validUsername, validPassword) shouldBe Result.Success(userId)

            // TODO: Finish
        }
    }

    @Nested
    inner class FailedRegistration {
        @Test
        fun `Given an existing user, when registering a new user with the same username, UsernameNotAvailableError should be returned`() {
            every { userTable.getUserByUsername(validUsername) } returns mockk()

            registerUser("username", "password").shouldBeTypeOf<Result.UsernameNotAvailableError>()
        }

        @Test
        fun `When registering a new user with invalid credentials, CredentialsFormatError should be returned with appropriate members`() {
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
