package com.bitwiserain.pbbg.domain.model.itemdetails

import kotlinx.serialization.Serializable

sealed class ItemHistoryInfo {
    abstract val enum: ItemHistoryInfoEnum

    @Serializable
    class CreatedInMarket : ItemHistoryInfo() {
        override val enum: ItemHistoryInfoEnum get() = ItemHistoryInfoEnum.CREATED_IN_MARKET
    }
    @Serializable
    class CreatedWithUser(val userId: Int) : ItemHistoryInfo() {
        override val enum: ItemHistoryInfoEnum get() = ItemHistoryInfoEnum.CREATED_WITH_USER
    }
    @Serializable
    class FirstMined(val userId: Int) : ItemHistoryInfo() {
        override val enum: ItemHistoryInfoEnum get() = ItemHistoryInfoEnum.FIRST_MINED
    }
}
