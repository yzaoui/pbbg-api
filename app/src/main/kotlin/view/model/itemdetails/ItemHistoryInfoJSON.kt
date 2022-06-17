package com.bitwiserain.pbbg.app.view.model.itemdetails

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ItemHistoryInfoJSON(
    @SerialName("type") val type: String,
    @SerialName("userId") val userId: Int?
)
