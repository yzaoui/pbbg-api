package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.BCryptHelper
import com.bitwiserain.pbbg.PASSWORD_REGEX
import com.bitwiserain.pbbg.PASSWORD_REGEX_DESCRIPTION
import com.bitwiserain.pbbg.USERNAME_REGEX
import com.bitwiserain.pbbg.USERNAME_REGEX_DESCRIPTION
import com.bitwiserain.pbbg.db.repository.DexTable
import com.bitwiserain.pbbg.db.repository.InventoryTable
import com.bitwiserain.pbbg.db.repository.ItemHistoryTable
import com.bitwiserain.pbbg.db.repository.MaterializedItemTable
import com.bitwiserain.pbbg.db.repository.SquadTable
import com.bitwiserain.pbbg.db.repository.UnitForm
import com.bitwiserain.pbbg.db.repository.UnitTable
import com.bitwiserain.pbbg.db.repository.UserStatsTable
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.db.repository.farm.PlotTable
import com.bitwiserain.pbbg.db.repository.market.MarketInventoryTable
import com.bitwiserain.pbbg.db.repository.market.MarketTable
import com.bitwiserain.pbbg.domain.model.MaterializedItem
import com.bitwiserain.pbbg.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistory
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistoryInfo
import com.bitwiserain.pbbg.domain.usecase.RegisterUserUC.Result
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
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
        object UsernameNotAvailableError : Result

        /**
         * The username or password don't match the required formats.
         */
        data class CredentialsFormatError(val usernameError: String?, val passwordError: String?) : Result
    }
}

class RegisterUserUCImpl(private val db: Database, private val clock: Clock, private val dexTable: DexTable, private val squadTable: SquadTable) : RegisterUserUC {

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

        return transaction(db) {
            /* Make sure username is available */
            if (UserTable.getUserByUsername(username) != null) return@transaction Result.UsernameNotAvailableError

            val now = clock.instant().truncatedTo(ChronoUnit.SECONDS)

            /* Create user */
            val userId = UserTable.createUserAndGetId(
                username = username,
                passwordHash = BCryptHelper.hashPassword(password)
            )

            /* Create user stats */
            UserStatsTable.createUserStats(userId)

            /* Add ice pick, apple saplings, tomato seeds to inventory */
            listOf(MaterializedItem.IcePick, MaterializedItem.AppleSapling(2), MaterializedItem.TomatoSeed(5)).forEach { item ->
                val itemId = MaterializedItemTable.insertItemAndGetId(item)
                ItemHistoryTable.insertItemHistory(
                    itemId = itemId,
                    itemHistory = ItemHistory(
                        date = now,
                        info = ItemHistoryInfo.CreatedWithUser(userId)
                    )
                )
                InventoryTable.insertItem(userId, itemId, item.base)
                dexTable.insertDiscovered(userId, item.enum)
            }

            /* Create user's market */
            val marketId = MarketTable.createMarketAndGetId(userId)

            // Fill market with three pickaxe types
            listOf(MaterializedItem.PlusPickaxe, MaterializedItem.CrossPickaxe, MaterializedItem.SquarePickaxe).forEach { item ->
                val itemId = MaterializedItemTable.insertItemAndGetId(item)
                MarketInventoryTable.insertItem(marketId, itemId)
                ItemHistoryTable.insertItemHistory(
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
                UnitTable.insertUnitAndGetId(it)
            }.also {
                // Add them to new user's squad
                squadTable.insertUnits(userId, it)
            }

            /* Create user farm */
            PlotTable.createAndGetEmptyPlot(userId)

            return@transaction Result.Success(userId)
        }
    }
}
