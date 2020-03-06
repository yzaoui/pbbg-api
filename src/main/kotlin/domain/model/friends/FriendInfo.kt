package com.bitwiserain.pbbg.domain.model.friends

data class FriendInfo(
    val userId: Int,
    val username: String,
    val friendship: Friendship
)
