package com.bitwiserain.pbbg.view.model.itemdetails

class ItemHistoryInfoJSON(
    val type: String,
    val user: UserInfoJSON?
) {
    class UserInfoJSON(
        val id: Int,
        val name: String
    )
}
