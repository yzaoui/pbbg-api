package com.bitwiserain.pbbg.app.view.model.friends

import com.google.gson.annotations.SerializedName

data class FriendsJSON(
    @SerializedName("friendInfos") val friendInfos: List<FriendInfoJSON>
)
