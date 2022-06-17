package com.bitwiserain.pbbg.app.view.model.itemdetails

import com.bitwiserain.pbbg.app.view.model.MaterializedItemJSON
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ItemDetailsJSON(
    @SerialName("item") val item: MaterializedItemJSON,
    @SerialName("history")  val history: List<ItemHistoryJSON>,
    @SerialName("linkedUserInfo") val linkedUserInfo: Map<Int, String>
)
