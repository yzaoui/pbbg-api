package com.bitwiserain.pbbg.view.model.dex

import com.bitwiserain.pbbg.view.model.BaseItemJSON
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Client equivalent of [com.bitwiserain.pbbg.domain.model.dex.DexItems].
 *
 * @property discoveredItems The items that the user has discovered, associated by item ID.
 * @property lastItemId See [com.bitwiserain.pbbg.domain.model.dex.DexItems.lastItemId]
 */
data class DexItemsJSON(
    @SerializedName("discoveredItems") val discoveredItems: SortedMap<Int, BaseItemJSON>,
    @SerializedName("lastItemId") val lastItemId: Int
)
