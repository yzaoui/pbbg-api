package com.bitwiserain.pbbg.domain.model

import domain.model.Equipment

/**
 * Representation of player's inventory.
 */
class Inventory(
    val items: List<Item>,
    val equipment: Equipment
)
