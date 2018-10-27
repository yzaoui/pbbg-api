package com.bitwiserain.pbbg.db.usecase

import at.favre.lib.crypto.bcrypt.BCrypt
import com.bitwiserain.pbbg.CharUnit
import com.bitwiserain.pbbg.PASSWORD_REGEX
import com.bitwiserain.pbbg.addUnitToSquad
import com.bitwiserain.pbbg.db.model.User
import com.bitwiserain.pbbg.db.repository.EquipmentTable
import com.bitwiserain.pbbg.db.repository.InventoryTable
import com.bitwiserain.pbbg.db.repository.UserStatsTable
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.model.Item
import com.bitwiserain.pbbg.domain.model.UserStats
import com.bitwiserain.pbbg.domain.usecase.IllegalPasswordException
import com.bitwiserain.pbbg.domain.usecase.UnconfirmedNewPasswordException
import com.bitwiserain.pbbg.domain.usecase.UserUC
import com.bitwiserain.pbbg.domain.usecase.WrongCurrentPasswordException
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
        val userId = UserTable.insertAndGetId {
            it[UserTable.username] = username
            it[UserTable.passwordHash] = BCrypt.withDefaults().hash(12, password.toByteArray())
        }

        UserStatsTable.insert {
            it[UserStatsTable.userId] = userId
        }

        EquipmentTable.insert {
            it[EquipmentTable.userId] = userId
        }

        // TODO: Temporarily giving user all three pickaxes on account creation
        listOf(Item.Pickaxe.PlusPickaxe(equipped = false), Item.Pickaxe.CrossPickaxe(equipped = false), Item.Pickaxe.SquarePickaxe(equipped = false)).forEach { pickaxe ->
            InventoryTable.insert {
                it[InventoryTable.userId] = userId
                it[InventoryTable.item] = pickaxe.enum
                it[InventoryTable.equipped] = false
            }

            Unit
        }

        // TODO: Temporarily give users a default unit
        addUnitToSquad(userId, CharUnit.IceCreamWizard(8, 8, 1, 1))

        userId.value
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
        UserStatsTable.select { UserStatsTable.userId.eq(userId) }
            .single()
            .toUserStats()
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

    private fun ResultRow.toUserStats(): UserStats = UserStats(this[UserStatsTable.miningExp])
}
