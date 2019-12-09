package com.bitwiserain.pbbg.db.model

import io.ktor.auth.Principal
import java.util.*

/**
 * Represents user account.
 *
 * @property id Unique identifier for this user.
 * @property username Unique name chosen by the user.
 * @property passwordHash The user's password hashed to be stored.
 */
data class User( // TODO: This class is being used across multiple layers
    val id: Int,
    val username: String,
    val passwordHash: ByteArray
) : Principal {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (username != other.username) return false
        if (!Arrays.equals(passwordHash, other.passwordHash)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + username.hashCode()
        result = 31 * result + Arrays.hashCode(passwordHash)
        return result
    }
}
