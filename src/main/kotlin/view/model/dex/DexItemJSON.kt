package com.bitwiserain.pbbg.view.model.dex

import com.bitwiserain.pbbg.view.model.BaseItemJSON
import com.google.gson.annotations.SerializedName

sealed class DexItemJSON(
    @SerializedName("type") val type: String
) {
    data class DiscoveredDexItemJSON(
        @SerializedName("baseItem") val baseItem: BaseItemJSON
    ) : DexItemJSON("discovered")

    data class UndiscoveredDexItemJSON(
        @SerializedName("id") val id: Int
    ) : DexItemJSON("undiscovered")
}
