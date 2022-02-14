package com.bitwiserain.pbbg.app.domain.model.friends

data class FriendInfo(
    val userId: Int,
    val username: String,
    val friendship: Friendship
)
