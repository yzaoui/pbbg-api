package com.bitwiserain.pbbg.app.domain.model.itemdetails

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class ItemHistoryInfo {
    interface HasUserId {
        val userId: Int
    }

    @Serializable
    @SerialName("created-market")
    data object CreatedInMarket : ItemHistoryInfo()

    @Serializable
    @SerialName("created-user")
    data class CreatedWithUser(override val userId: Int) : ItemHistoryInfo(), HasUserId

    @Serializable
    @SerialName("first-mined")
    data class FirstMined(override val userId: Int) : ItemHistoryInfo(), HasUserId

    @Serializable
    @SerialName("first-harvested")
    data class FirstHarvested(override val userId: Int) : ItemHistoryInfo(), HasUserId
}
