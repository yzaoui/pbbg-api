package com.bitwiserain.pbbg.view.model

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Client equivalent of [com.bitwiserain.pbbg.domain.model.dex.Dex].
 *
 * @property discoveredItems The IDs of the items that the user has discovered.
 * @property lastItemIsDiscovered See [com.bitwiserain.pbbg.domain.model.dex.Dex.lastItemIsDiscovered]
 */
data class DexJSON(
    @SerializedName("discoveredItems") val discoveredItems: SortedSet<Int>,
    @SerializedName("lastItemIsDiscovered") val lastItemIsDiscovered: Boolean
)
