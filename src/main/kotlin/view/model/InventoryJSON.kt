package com.bitwiserain.pbbg.view.model

import com.google.gson.annotations.SerializedName

data class InventoryJSON(
    @SerializedName("inventoryEntries") val inventoryEntries: List<InventoryItemJSON>
)
