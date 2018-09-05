package com.bitwiserain.pbbg.domain.model

/**
 * Representation of an individual inventory entry.
 *
 * @property item The kind of item this entry represents.
 * @property quantity The held quantity of this [item].
 */
data class InventoryEntry(val item: Item, val quantity: Int)
