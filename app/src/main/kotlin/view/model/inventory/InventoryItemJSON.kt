package com.bitwiserain.pbbg.app.view.model.inventory

import com.bitwiserain.pbbg.app.view.model.MaterializedItemJSON
import com.google.gson.annotations.SerializedName

data class InventoryItemJSON(
    @SerializedName("item") val item: MaterializedItemJSON,
    @SerializedName("equipped") val equipped: Boolean?
)
