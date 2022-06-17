package com.bitwiserain.pbbg.app.view.model.inventory

import com.bitwiserain.pbbg.app.view.model.MaterializedItemJSON
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InventoryItemJSON(
    @SerialName("item") val item: MaterializedItemJSON,
    @SerialName("equipped") val equipped: Boolean?
)
