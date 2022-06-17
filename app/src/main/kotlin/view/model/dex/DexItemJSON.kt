package com.bitwiserain.pbbg.app.view.model.dex

import com.bitwiserain.pbbg.app.view.model.BaseItemJSON
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator("type")
sealed class DexItemJSON {
    @Serializable
    @SerialName("discovered")
    data class DiscoveredDexItemJSON(
        @SerialName("baseItem") val baseItem: BaseItemJSON
    ) : DexItemJSON()

    @Serializable
    @SerialName("undiscovered")
    data class UndiscoveredDexItemJSON(
        @SerialName("id") val id: Int
    ) : DexItemJSON()
}
