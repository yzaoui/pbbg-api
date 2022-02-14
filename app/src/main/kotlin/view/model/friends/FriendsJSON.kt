package com.bitwiserain.pbbg.view.model.friends

import com.google.gson.annotations.SerializedName

data class FriendsJSON(
    @SerializedName("friendInfos") val friendInfos: List<FriendInfoJSON>
)
