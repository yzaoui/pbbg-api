package com.bitwiserain.pbbg.app.domain.model.mine

/**
 * Represents a mine entity, with dimensions and contents.
 *
 * @property width The width of this mine.
 * @property height The height of this mine.
 * @property grid Maps all non-empty mine cells' coordinates to their containing entity.
 */
data class Mine(
    val width: Int,
    val height: Int,
    val grid: Map<Pair<Int, Int>, MineEntity>,
    val mineType: MineType
)
