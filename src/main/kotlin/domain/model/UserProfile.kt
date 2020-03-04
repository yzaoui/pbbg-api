package com.bitwiserain.pbbg.domain.model

import com.bitwiserain.pbbg.domain.model.friends.Friendship

data class UserProfile(
    val username: String,
    val friendship: Friendship?
)
