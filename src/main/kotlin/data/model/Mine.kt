package miner.data.model

/**
 * Represents a mine entity.
 */
data class Mine(
    val width: Int,
    val height: Int,
    val grid: Map<Pair<Int, Int>, MineEntity>
)
