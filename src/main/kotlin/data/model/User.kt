package miner.data.model

import java.util.*

data class User(
    val id: Int,
    val username: String,
    val passwordHash: ByteArray
) {
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
