package com.bitwiserain.pbbg.domain.model.itemdetails

import kotlinx.serialization.Serializable

sealed class ItemHistoryInfo {
    abstract val enum: ItemHistoryInfoEnum

    interface HasUserId {
        val userId: Int
    }

    @Serializable
    object CreatedInMarket : ItemHistoryInfo() {
        override val enum = ItemHistoryInfoEnum.CREATED_IN_MARKET
    }

    @Serializable
    class CreatedWithUser(override val userId: Int) : ItemHistoryInfo(), HasUserId {
        override val enum = ItemHistoryInfoEnum.CREATED_WITH_USER
    }

    @Serializable
    class FirstMined(override val userId: Int) : ItemHistoryInfo(), HasUserId {
        override val enum = ItemHistoryInfoEnum.FIRST_MINED
    }

    @Serializable
    class FirstHarvested(override val userId: Int) : ItemHistoryInfo(), HasUserId {
        override val enum = ItemHistoryInfoEnum.FIRST_HARVESTED
    }
}
