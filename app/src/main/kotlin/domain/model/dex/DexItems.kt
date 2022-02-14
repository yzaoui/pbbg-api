package com.bitwiserain.pbbg.app.domain.model.dex

import com.bitwiserain.pbbg.app.domain.model.ItemEnum

/**
 * The user's items dex, which lists all item types that the user has obtained at some point.
 *
 * @property discoveredItems The items that the user has obtained at some point.
 * @property lastItemId The last possible item's ID.
 */
data class DexItems(
    val discoveredItems: Set<ItemEnum>,
    val lastItemId: Int
)
