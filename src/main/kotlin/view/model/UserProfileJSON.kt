package com.bitwiserain.pbbg.view.model

import com.google.gson.annotations.SerializedName

data class UserProfileJSON(
    @SerializedName("username") val username: String,
    @SerializedName("friends") val friends: Boolean?
)
