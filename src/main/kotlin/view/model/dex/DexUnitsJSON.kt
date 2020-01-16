package com.bitwiserain.pbbg.view.model.dex

import com.bitwiserain.pbbg.view.model.MyUnitEnumJSON
import com.google.gson.annotations.SerializedName
import java.util.*

data class DexUnitsJSON(
    @SerializedName("discoveredUnits") val discoveredUnits: SortedMap<Int, MyUnitEnumJSON>,
    @SerializedName("lastUnitId") val lastUnitId: Int
)
