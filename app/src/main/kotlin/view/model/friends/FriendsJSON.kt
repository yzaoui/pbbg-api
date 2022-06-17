package com.bitwiserain.pbbg.app.view.model.friends

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FriendsJSON(
    @SerialName("friendInfos") val friendInfos: List<FriendInfoJSON>
)
