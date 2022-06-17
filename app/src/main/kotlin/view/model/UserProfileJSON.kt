package com.bitwiserain.pbbg.app.view.model

import com.bitwiserain.pbbg.app.view.model.friends.FriendshipJSON
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileJSON(
    @SerialName("id") val id: Int,
    @SerialName("username") val username: String,
    @SerialName("joinedDate") val joinedDate: String,
    @SerialName("friendship") val friendship: FriendshipJSON?
)
