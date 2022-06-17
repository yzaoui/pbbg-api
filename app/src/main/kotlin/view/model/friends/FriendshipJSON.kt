package com.bitwiserain.pbbg.app.view.model.friends

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class FriendshipJSON {
    @SerialName("none") NONE,
    @SerialName("request-sent") REQUEST_SENT,
    @SerialName("request-received") REQUEST_RECEIVED,
    @SerialName("confirmed") CONFIRMED
}
