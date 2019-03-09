package com.bitwiserain.pbbg.domain.model.dex

import com.bitwiserain.pbbg.domain.model.ItemEnum

/**
 * The user's dex, which lists all item types that the user has obtained at some point.
 *
 * @property discoveredItems The items that the user has obtained at some point.
 * @property lastItemIsDiscovered Whether the last possible entry in the dex has been discovered.
 */
data class Dex(
    val discoveredItems: Set<ItemEnum>,
    val lastItemIsDiscovered: Boolean
)
