package com.bitwiserain.pbbg.domain.model

/**
 * Representation of player's inventory.
 *
 * @property entries A list of individual inventory item representations.
 */
data class Inventory(val entries: List<InventoryEntry>)
