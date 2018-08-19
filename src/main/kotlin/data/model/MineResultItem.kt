package data.model

import miner.data.model.Item

/**
 * Represents the result of a mining operation.
 */
data class MineResultItem(
    val item: Item,
    val amount: Int
)
