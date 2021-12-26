package com.bitwiserain.pbbg.domain.model

import com.bitwiserain.pbbg.domain.model.friends.Friendship
import java.time.Instant

data class UserProfile(
    val id: Int,
    val username: String,
    val joinedInstant: Instant,
    val friendship: Friendship?
)
