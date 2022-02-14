package com.bitwiserain.pbbg.app.view.model.friends

import com.google.gson.annotations.SerializedName

enum class FriendshipJSON {
    @SerializedName("none") NONE,
    @SerializedName("request-sent") REQUEST_SENT,
    @SerializedName("request-received") REQUEST_RECEIVED,
    @SerializedName("confirmed") CONFIRMED
}
