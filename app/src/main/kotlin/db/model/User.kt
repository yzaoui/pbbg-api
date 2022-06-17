package com.bitwiserain.pbbg.app.db.model

import io.ktor.server.auth.Principal
import java.time.Instant

/**
 * Represents user account.
 *
 * @property id Unique identifier for this user.
 * @property username Unique name chosen by the user.
 * @property passwordHash The user's password hashed to be stored.
 * @property joinedInstant The time the user joined.
 */
data class User( // TODO: This class is being used across multiple layers
    val id: Int,
    val username: String,
    val passwordHash: ByteArray,
    val joinedInstant: Instant,
) : Principal {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (username != other.username) return false
        if (!passwordHash.contentEquals(other.passwordHash)) return false
        if (joinedInstant != other.joinedInstant) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + username.hashCode()
        result = 31 * result + passwordHash.contentHashCode()
        result = 31 * result + joinedInstant.hashCode()
        return result
    }
}
