package com.bitwiserain.pbbg.app.view.model.dex

import com.bitwiserain.pbbg.app.view.model.MyUnitEnumJSON
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DexUnitsJSON(
    @SerialName("discoveredUnits") val discoveredUnits: Map<Int, MyUnitEnumJSON>,
    @SerialName("lastUnitId") val lastUnitId: Int
)
