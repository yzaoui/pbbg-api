package com.bitwiserain.pbbg.domain.model.itemdetails

import kotlinx.serialization.Serializable

sealed class ItemHistoryInfo {
    abstract val enum: ItemHistoryInfoEnum

    interface HasUserId {
        val userId: Int
    }

    @Serializable
    class CreatedInMarket : ItemHistoryInfo() {
        override val enum: ItemHistoryInfoEnum get() = ItemHistoryInfoEnum.CREATED_IN_MARKET
    }
    @Serializable
    class CreatedWithUser(override val userId: Int) : ItemHistoryInfo(), HasUserId {
        override val enum: ItemHistoryInfoEnum get() = ItemHistoryInfoEnum.CREATED_WITH_USER
    }
    @Serializable
    class FirstMined(override val userId: Int) : ItemHistoryInfo(), HasUserId {
        override val enum: ItemHistoryInfoEnum get() = ItemHistoryInfoEnum.FIRST_MINED
    }
}
