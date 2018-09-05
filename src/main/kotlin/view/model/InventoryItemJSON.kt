package com.bitwiserain.pbbg.view.model

import com.google.gson.annotations.SerializedName

data class InventoryItemJSON(
    @SerializedName("item") val item: ItemJSON,
    @SerializedName("quantity") val quantity: Int
)
