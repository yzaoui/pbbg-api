package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.*
import com.bitwiserain.pbbg.db.repository.*
import com.bitwiserain.pbbg.db.repository.farm.PlotTable
import com.bitwiserain.pbbg.db.repository.market.MarketInventoryTable
import com.bitwiserain.pbbg.db.repository.market.MarketTable
import com.bitwiserain.pbbg.domain.model.MaterializedItem
import com.bitwiserain.pbbg.domain.model.MyUnitEnum.*
import com.bitwiserain.pbbg.domain.model.UserStats
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistory
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistoryInfo
import com.bitwiserain.pbbg.domain.usecase.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Clock
import java.time.temporal.ChronoUnit

class UserUCImpl(private val db: Database, private val clock: Clock) : UserUC {
    override fun registerUser(username: String, password: String): Int = transaction(db) {
        /* Make sure username is available */
        if (UserTable.getUserByUsername(username) != null) throw UsernameNotAvailableException(username)

        /* Make sure username & password are valid */
        run {
            val usernameInvalid = !username.matches(USERNAME_REGEX.toRegex())
            val passwordInvalid = !password.matches(PASSWORD_REGEX.toRegex())

            if (usernameInvalid || passwordInvalid) throw CredentialsFormatException(
                usernameError = if (usernameInvalid) USERNAME_REGEX_DESCRIPTION else null,
                passwordError = if (passwordInvalid) PASSWORD_REGEX_DESCRIPTION else null
            )
        }

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
            DexTable.insertDiscovered(userId, item.enum)
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
            UnitForm(ICE_CREAM_WIZARD, ICE_CREAM_WIZARD.baseHP, ICE_CREAM_WIZARD.baseAtk, ICE_CREAM_WIZARD.baseDef, ICE_CREAM_WIZARD.baseInt, ICE_CREAM_WIZARD.baseRes),
            UnitForm(CARPSHOOTER, CARPSHOOTER.baseHP, CARPSHOOTER.baseAtk, CARPSHOOTER.baseDef, CARPSHOOTER.baseInt, CARPSHOOTER.baseRes),
            UnitForm(TWOLIP, TWOLIP.baseHP, TWOLIP.baseAtk, TWOLIP.baseDef, TWOLIP.baseInt, TWOLIP.baseRes)
        ).map {
            // Create initial units
            UnitTable.insertUnitAndGetId(it)
        }.also {
            // Add them to new user's squad
            SquadTable.insertUnits(userId, it)
        }

        /* Create user farm */
        PlotTable.createAndGetEmptyPlot(userId)

        return@transaction userId
    }

    override fun getUserIdByCredentials(username: String, password: String): Int? {
        val user = transaction(db) {
            UserTable.getUserByUsername(username)
        }

        return if (user != null && BCryptHelper.verifyPassword(password, user.passwordHash)) {
            user.id
        } else {
            null
        }
    }

    override fun getUserStatsByUserId(userId: Int): UserStats = transaction(db) {
        // TODO: Consider checking if user exists
        UserStatsTable.getUserStats(userId)
    }

    override fun changePassword(userId: Int, currentPassword: String, newPassword: String, confirmNewPassword: String): Unit = transaction(db) {
        // TODO: Consider checking if user exists
        val expectedPasswordHash = UserTable.select { UserTable.id.eq(userId) }
            .map { it[UserTable.passwordHash] }
            .single()

        // Make sure current password matches
        if (!BCryptHelper.verifyPassword(currentPassword, expectedPasswordHash)) throw WrongCurrentPasswordException()

        // Make sure new password is actually new
        if (currentPassword == newPassword) throw NewPasswordNotNewException()

        // Make sure new password was typed twice correctly
        if (newPassword != confirmNewPassword) throw UnconfirmedNewPasswordException()

        // Make sure new password fits format requirement
        if (!newPassword.matches(PASSWORD_REGEX.toRegex())) throw IllegalPasswordException()

        // At this point, new password is legal, so update
        UserTable.update({ UserTable.id.eq(userId) }) {
            it[UserTable.passwordHash] = BCryptHelper.hashPassword(newPassword)
        }
    }
}
