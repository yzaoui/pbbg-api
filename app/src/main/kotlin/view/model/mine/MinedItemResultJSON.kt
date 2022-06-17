package com.bitwiserain.pbbg.app.view.model.mine

import com.bitwiserain.pbbg.app.view.model.MaterializedItemJSON
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MinedItemResultJSON(
    @SerialName("item") val item: MaterializedItemJSON,
    @SerialName("expPerIndividualItem") val expPerIndividualItem: Int
)
