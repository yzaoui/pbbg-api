package com.bitwiserain.pbbg.app.view.model.friends

import com.google.gson.annotations.SerializedName

data class FriendInfoJSON(
    @SerializedName("userId") val userId: Int,
    @SerializedName("username") val username: String,
    @SerializedName("friendship") val friendship: FriendshipJSON
)
