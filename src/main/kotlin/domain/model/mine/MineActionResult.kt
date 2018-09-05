package com.bitwiserain.pbbg.domain.model.mine

import com.bitwiserain.pbbg.domain.model.Item

/**
 * Represents the result of a mining operation.
 *
 * @property item The item obtained from the prior mining action.
 * @property quantity The quantity of [item] obtained from this action.
 */
data class MineActionResult(
    val item: Item,
    val quantity: Int
)
