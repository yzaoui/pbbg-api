package com.bitwiserain.pbbg.domain.model.itemdetails

sealed class ItemHistoryInfo {
    object CreatedInMarket : ItemHistoryInfo()
    class CreatedWithUser(val user: UserInfo) : ItemHistoryInfo()
    class Mined(val user: UserInfo) : ItemHistoryInfo()

    data class UserInfo(
        val id: Int,
        val name: String
    )
}
