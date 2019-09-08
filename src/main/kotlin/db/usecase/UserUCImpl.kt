package com.bitwiserain.pbbg.db.usecase

import at.favre.lib.crypto.bcrypt.BCrypt
import com.bitwiserain.pbbg.PASSWORD_REGEX
import com.bitwiserain.pbbg.db.form.MyUnitForm
import com.bitwiserain.pbbg.db.model.User
import com.bitwiserain.pbbg.db.repository.*
import com.bitwiserain.pbbg.db.repository.market.MarketInventoryTable
import com.bitwiserain.pbbg.db.repository.market.MarketTable
import com.bitwiserain.pbbg.domain.model.MaterializedItem
import com.bitwiserain.pbbg.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.domain.model.UserStats
import com.bitwiserain.pbbg.domain.usecase.IllegalPasswordException
import com.bitwiserain.pbbg.domain.usecase.UnconfirmedNewPasswordException
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.domain.usecase.WrongCurrentPasswordException
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class UserUCImpl(private val db: Database) : UserUC {
    override fun getUserById(userId: Int): User? = transaction(db) {
        UserTable.select { UserTable.id.eq(userId) }
            .map { User(it[UserTable.id].value, it[UserTable.username], it[UserTable.passwordHash]) }
            .singleOrNull()
    }

    override fun getUserByUsername(username: String): User? = transaction(db) {
        UserTable.select { UserTable.username.eq(username) }
            .map { User(it[UserTable.id].value, it[UserTable.username], it[UserTable.passwordHash]) }
            .singleOrNull()
    }

    override fun usernameAvailable(username: String): Boolean {
        return getUserByUsername(username) == null
    }

    override fun registerUser(username: String, password: String): Int = transaction(db) {
        val userId = UserTable.createUserAndGetId(
            username = username,
            passwordHash = BCrypt.withDefaults().hash(12, password.toByteArray())
        )

        UserStatsTable.insert {
            it[UserStatsTable.userId] = userId
        }

        // TODO: Temporarily giving user all three pickaxes on account creation
        listOf(MaterializedItem.PlusPickaxe, MaterializedItem.CrossPickaxe, MaterializedItem.SquarePickaxe).forEach { pickaxe ->
            val itemId = MaterializedItemTable.insertItemAndGetId(pickaxe)
            InventoryTable.insertItem(userId, itemId, pickaxe.base)
            DexTable.insertDiscovered(userId, pickaxe.enum)
        }

        /* Market */
        val marketId = MarketTable.insertAndGetId {
            it[MarketTable.userId] = userId
        }
        listOf<MaterializedItem>(MaterializedItem.PlusPickaxe).forEach { item ->
            val itemId = MaterializedItemTable.insertItemAndGetId(item)
            MarketInventoryTable.insertItem(marketId, itemId)
        }

        SquadTable.insertAllies(userId, listOf(
            MyUnitForm(MyUnitEnum.ICE_CREAM_WIZARD, 9, 1, 1),
            MyUnitForm(MyUnitEnum.CARPSHOOTER, 8, 1, 2),
            MyUnitForm(MyUnitEnum.TWOLIP, 11, 2, 1)
        ))

        return@transaction userId.value
    }

    override fun getUserIdByCredentials(username: String, password: String): Int? {
        val user = getUserByUsername(username)
        return if (user != null && BCrypt.verifyer().verify(password.toByteArray(), user.passwordHash).verified) {
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
        val currentPasswordHash = UserTable.select { UserTable.id.eq(userId) }
            .map { it[UserTable.passwordHash] }
            .single()

        // Make sure current password matches
        if (!BCrypt.verifyer().verify(currentPassword.toByteArray(), currentPasswordHash).verified) throw WrongCurrentPasswordException()

        // Make sure new password was typed twice correctly
        if (newPassword != confirmNewPassword) throw UnconfirmedNewPasswordException()

        // Make sure new password fits format requirement
        if (!newPassword.matches(PASSWORD_REGEX.toRegex())) throw IllegalPasswordException()

        // At this point, new password is legal, so update
        UserTable.update({ UserTable.id.eq(userId) }) {
            it[UserTable.passwordHash] = BCrypt.withDefaults().hash(12, newPassword.toByteArray())
        }
    }
}
