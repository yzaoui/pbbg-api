package miner.domain.usecase

import miner.data.UserTable
import miner.data.model.User
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

interface UserUseCase {
    fun getUserById(userId: Int): User?
}

class UserUseCaseImpl : UserUseCase {
    override fun getUserById(userId: Int): User? = transaction {
        UserTable.select { UserTable.id.eq(userId) }
            .mapNotNull {
                User(it[UserTable.id].value, it[UserTable.username], it[UserTable.passwordHash])
            }
            .singleOrNull()
    }
}
