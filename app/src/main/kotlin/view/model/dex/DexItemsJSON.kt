package com.bitwiserain.pbbg.app.view.model.dex

import com.bitwiserain.pbbg.app.view.model.BaseItemJSON
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Client equivalent of [com.bitwiserain.pbbg.app.domain.model.dex.DexItems].
 *
 * @property discoveredItems The items that the user has discovered, associated by item ID.
 * @property lastItemId See [com.bitwiserain.pbbg.app.domain.model.dex.DexItems.lastItemId]
 */
@Serializable
data class DexItemsJSON(
    @SerialName("discoveredItems") val discoveredItems: Map<Int, BaseItemJSON>,
    @SerialName("lastItemId") val lastItemId: Int
)
