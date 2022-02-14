package com.bitwiserain.pbbg.app.view.model

import com.bitwiserain.pbbg.app.view.model.friends.FriendshipJSON
import com.google.gson.annotations.SerializedName

data class UserProfileJSON(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("joinedDate") val joinedDate: String,
    @SerializedName("friendship") val friendship: FriendshipJSON?
)
