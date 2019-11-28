package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.*
import com.bitwiserain.pbbg.db.form.MyUnitForm
import com.bitwiserain.pbbg.db.model.User
import com.bitwiserain.pbbg.db.repository.*
import com.bitwiserain.pbbg.db.repository.market.MarketInventoryTable
import com.bitwiserain.pbbg.db.repository.market.MarketTable
import com.bitwiserain.pbbg.domain.model.MaterializedItem
import com.bitwiserain.pbbg.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.domain.model.UserStats
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistory
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistoryInfo
import com.bitwiserain.pbbg.domain.usecase.*
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.temporal.ChronoUnit

class UserUCImpl(private val db: Database) : UserUC {
    override fun getUserById(userId: Int): User? = transaction(db) {
        UserTable.select { UserTable.id.eq(userId) }
            .map { User(it[UserTable.id].value, it[UserTable.username], it[UserTable.passwordHash]) }
            .singleOrNull()
    }

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

        val now = Instant.now().truncatedTo(ChronoUnit.SECONDS)

        val userId = UserTable.createUserAndGetId(
            username = username,
            passwordHash = BCryptHelper.hashPassword(password)
        )

        UserStatsTable.insert {
            it[UserStatsTable.userId] = userId
        }

        listOf(MaterializedItem.IcePick).forEach { item ->
            val itemId = MaterializedItemTable.insertItemAndGetId(item)
            ItemHistoryTable.insertItemHistory(
                itemId = itemId.value,
                itemHistory = ItemHistory(
                    date = now,
                    info = ItemHistoryInfo.CreatedWithUser(userId.value)
                )
            )
            InventoryTable.insertItem(userId, itemId, item.base)
            DexTable.insertDiscovered(userId, item.enum)
        }

        /* Market */
        val marketId = MarketTable.insertAndGetId {
            it[MarketTable.userId] = userId
        }
        // Fill market with three pickaxe types
        listOf(MaterializedItem.PlusPickaxe, MaterializedItem.CrossPickaxe, MaterializedItem.SquarePickaxe).forEach { item ->
            val itemId = MaterializedItemTable.insertItemAndGetId(item)
            MarketInventoryTable.insertItem(marketId, itemId)
            ItemHistoryTable.insertItemHistory(
                itemId = itemId.value,
                itemHistory = ItemHistory(
                    date = now,
                    info = ItemHistoryInfo.CreatedInMarket()
                )
            )
        }

        listOf(
            MyUnitForm(MyUnitEnum.ICE_CREAM_WIZARD, 9, 1, 1),
            MyUnitForm(MyUnitEnum.CARPSHOOTER, 8, 1, 2),
            MyUnitForm(MyUnitEnum.TWOLIP, 11, 2, 1)
        ).map {
            // Create initial units
            UnitTable.insertUnitAndGetId(it)
        }.also {
            // Add them to new user's squad
            SquadTable.insertUnits(userId, it)
        }

        return@transaction userId.value
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
        UserStatsTable.getUserStats(EntityID(userId, UserTable))
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
