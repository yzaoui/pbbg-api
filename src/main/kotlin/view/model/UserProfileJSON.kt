package com.bitwiserain.pbbg.view.model

import com.bitwiserain.pbbg.view.model.friends.FriendshipJSON
import com.google.gson.annotations.SerializedName

data class UserProfileJSON(
    @SerializedName("username") val username: String,
    @SerializedName("friendship") val friendship: FriendshipJSON?
)
