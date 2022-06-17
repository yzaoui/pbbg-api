package com.bitwiserain.pbbg.app.view.model.itemdetails

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ItemHistoryJSON(
    @SerialName("date") val date: Long,
    @SerialName("info") val info: ItemHistoryInfoJSON
)
