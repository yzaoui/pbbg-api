package com.bitwiserain.pbbg.app.domain.usecase

import com.bitwiserain.pbbg.app.BCryptHelper
import com.bitwiserain.pbbg.app.PASSWORD_REGEX
import com.bitwiserain.pbbg.app.PASSWORD_REGEX_DESCRIPTION
import com.bitwiserain.pbbg.app.USERNAME_REGEX
import com.bitwiserain.pbbg.app.USERNAME_REGEX_DESCRIPTION
import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.DexTable
import com.bitwiserain.pbbg.app.db.repository.InventoryTable
import com.bitwiserain.pbbg.app.db.repository.ItemHistoryTable
import com.bitwiserain.pbbg.app.db.repository.MaterializedItemTable
import com.bitwiserain.pbbg.app.db.repository.SquadTable
import com.bitwiserain.pbbg.app.db.repository.UnitTable
import com.bitwiserain.pbbg.app.db.repository.UnitTable.UnitForm
import com.bitwiserain.pbbg.app.db.repository.UserStatsTable
import com.bitwiserain.pbbg.app.db.repository.UserTable
import com.bitwiserain.pbbg.app.db.repository.farm.PlotListTable
import com.bitwiserain.pbbg.app.db.repository.farm.PlotTable
import com.bitwiserain.pbbg.app.db.repository.market.MarketInventoryTable
import com.bitwiserain.pbbg.app.db.repository.market.MarketTable
import com.bitwiserain.pbbg.app.domain.model.MaterializedItem
import com.bitwiserain.pbbg.app.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.app.domain.model.itemdetails.ItemHistory
import com.bitwiserain.pbbg.app.domain.model.itemdetails.ItemHistoryInfo
import com.bitwiserain.pbbg.app.domain.usecase.RegisterUserUC.Result
import java.time.Clock
import java.time.temporal.ChronoUnit

/**
 * Registers a user, providing the user with default inventory items and units.
 */
interface RegisterUserUC {
    /**
     * @param username The user's username.
     * @param password The user's password.
     */
    operator fun invoke(username: String, password: String): Result

    sealed interface Result {
        /**
         * User registered successfully.
         */
        data class Success(val userId: Int) : Result

        /**
         * The username is not available.
         */
        data object UsernameNotAvailableError : Result

        /**
         * The username or password don't match the required formats.
         */
        data class CredentialsFormatError(val usernameError: String?, val passwordError: String?) : Result
    }
}

class RegisterUserUCImpl(
    private val transaction: Transaction,
    private val clock: Clock,
    private val dexTable: DexTable,
    private val inventoryTable: InventoryTable,
    private val itemHistoryTable: ItemHistoryTable,
    private val marketTable: MarketTable,
    private val marketInventoryTable: MarketInventoryTable,
    private val materializedItemTable: MaterializedItemTable,
    private val plotTable: PlotTable,
    private val plotListTable: PlotListTable,
    private val squadTable: SquadTable,
    private val unitTable: UnitTable,
    private val userTable: UserTable,
    private val userStatsTable: UserStatsTable,
) : RegisterUserUC {

    override fun invoke(username: String, password: String): Result {
        /* Make sure username & password are valid */
        run {
            val usernameInvalid = !username.matches(USERNAME_REGEX.toRegex())
            val passwordInvalid = !password.matches(PASSWORD_REGEX.toRegex())

            if (usernameInvalid || passwordInvalid) return Result.CredentialsFormatError(
                usernameError = if (usernameInvalid) USERNAME_REGEX_DESCRIPTION else null,
                passwordError = if (passwordInvalid) PASSWORD_REGEX_DESCRIPTION else null
            )
        }

        return transaction {
            /* Make sure username is available */
            if (userTable.getUserByUsername(username) != null) return@transaction Result.UsernameNotAvailableError

            val now = clock.instant().truncatedTo(ChronoUnit.SECONDS)

            /* Create user */
            val userId = userTable.createUserAndGetId(
                username = username,
                passwordHash = BCryptHelper.hashPassword(password),
                joinedInstant = now
            )

            /* Create user stats */
            userStatsTable.createUserStats(userId)

            /* Add ice pick, apple saplings, tomato seeds to inventory */
            listOf(MaterializedItem.IcePick, MaterializedItem.AppleSapling(2), MaterializedItem.TomatoSeed(5)).forEach { item ->
                val itemId = materializedItemTable.insertItemAndGetId(item)
                itemHistoryTable.insertItemHistory(
                    itemId = itemId,
                    itemHistory = ItemHistory(
                        date = now,
                        info = ItemHistoryInfo.CreatedWithUser(userId)
                    )
                )
                inventoryTable.insertItem(userId, itemId, item.base)
                dexTable.insertDiscovered(userId, item.enum)
            }

            /* Create user's market */
            val marketId = marketTable.createMarketAndGetId(userId)

            // Fill market with three pickaxe types
            listOf(MaterializedItem.PlusPickaxe, MaterializedItem.CrossPickaxe, MaterializedItem.SquarePickaxe).forEach { item ->
                val itemId = materializedItemTable.insertItemAndGetId(item)
                marketInventoryTable.insertItem(marketId, itemId)
                itemHistoryTable.insertItemHistory(
                    itemId = itemId,
                    itemHistory = ItemHistory(
                        date = now,
                        info = ItemHistoryInfo.CreatedInMarket
                    )
                )
            }

            /* Create user squad */
            listOf(
                UnitForm(MyUnitEnum.ICE_CREAM_WIZARD, MyUnitEnum.ICE_CREAM_WIZARD.baseHP, MyUnitEnum.ICE_CREAM_WIZARD.baseAtk, MyUnitEnum.ICE_CREAM_WIZARD.baseDef, MyUnitEnum.ICE_CREAM_WIZARD.baseInt, MyUnitEnum.ICE_CREAM_WIZARD.baseRes),
                UnitForm(MyUnitEnum.CARPSHOOTER, MyUnitEnum.CARPSHOOTER.baseHP, MyUnitEnum.CARPSHOOTER.baseAtk, MyUnitEnum.CARPSHOOTER.baseDef, MyUnitEnum.CARPSHOOTER.baseInt, MyUnitEnum.CARPSHOOTER.baseRes),
                UnitForm(MyUnitEnum.TWOLIP, MyUnitEnum.TWOLIP.baseHP, MyUnitEnum.TWOLIP.baseAtk, MyUnitEnum.TWOLIP.baseDef, MyUnitEnum.TWOLIP.baseInt, MyUnitEnum.TWOLIP.baseRes)
            ).map {
                // Create initial units
                unitTable.insertUnitAndGetId(it)
            }.also {
                // Add them to new user's squad
                squadTable.insertUnits(userId, it)
            }

            /* Create user farm */
            plotListTable.insertUser(userId)
            plotTable.createAndGetEmptyPlot(userId)

            return@transaction Result.Success(userId)
        }
    }
}
