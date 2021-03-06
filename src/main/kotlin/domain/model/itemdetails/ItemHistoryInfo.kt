package com.bitwiserain.pbbg.domain.model.itemdetails

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class ItemHistoryInfo {
    interface HasUserId {
        val userId: Int
    }

    @Serializable
    @SerialName("created-market")
    object CreatedInMarket : ItemHistoryInfo()

    @Serializable
    @SerialName("created-user")
    class CreatedWithUser(override val userId: Int) : ItemHistoryInfo(), HasUserId

    @Serializable
    @SerialName("first-mined")
    class FirstMined(override val userId: Int) : ItemHistoryInfo(), HasUserId

    @Serializable
    @SerialName("first-harvested")
    class FirstHarvested(override val userId: Int) : ItemHistoryInfo(), HasUserId
}
