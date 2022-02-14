package com.bitwiserain.pbbg.app.domain.model

/**
 * Representation of player's inventory.
 */
data class Inventory(
    val items: Map<Long, InventoryItem>
)
