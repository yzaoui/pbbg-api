package com.bitwiserain.pbbg.app.db.model

import com.bitwiserain.pbbg.app.domain.model.mine.MineEntity

/**
 * Represents the position and contents of a mine cell in the context of an existing mine.
 *
 * @property id Unique identifier of this cell.
 * @property x The x coordinate of this cell in the context of its container mine.
 * @property y The y coordinate of this cell in the context of its container mine.
 * @property mineEntity The contained entity of this cell.
 */
data class MineCell(
    val id: Int,
    val x: Int,
    val y: Int,
    val mineEntity: MineEntity
)
