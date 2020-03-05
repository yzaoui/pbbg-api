package com.bitwiserain.pbbg.view.model

import com.bitwiserain.pbbg.view.model.friends.FriendshipJSON
import com.google.gson.annotations.SerializedName

data class UserProfileJSON(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("friendship") val friendship: FriendshipJSON?
)
