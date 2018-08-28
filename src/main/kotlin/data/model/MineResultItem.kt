package data.model

import pbbg.data.model.Item

/**
 * Represents the result of a mining operation.
 */
data class MineResultItem(
    val item: Item,
    val amount: Int
)
