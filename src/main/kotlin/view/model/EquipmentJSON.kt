package com.bitwiserain.pbbg.view.model

import com.google.gson.annotations.SerializedName

data class EquipmentJSON(
    @SerializedName("pickaxe") val pickaxe: ItemJSON?
)
