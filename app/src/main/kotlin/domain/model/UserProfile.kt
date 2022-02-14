package com.bitwiserain.pbbg.app.domain.model

import com.bitwiserain.pbbg.app.domain.model.friends.Friendship
import java.time.Instant

data class UserProfile(
    val id: Int,
    val username: String,
    val joinedInstant: Instant,
    val friendship: Friendship?
)
