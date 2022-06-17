package com.bitwiserain.pbbg.app.view.model.friends

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FriendInfoJSON(
    @SerialName("userId") val userId: Int,
    @SerialName("username") val username: String,
    @SerialName("friendship") val friendship: FriendshipJSON
)
