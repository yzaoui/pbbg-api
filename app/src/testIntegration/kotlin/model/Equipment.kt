package com.bitwiserain.pbbg.app.testintegration.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Equipment(
    @SerialName("pickaxe") val pickaxe: InventoryItem?
)
