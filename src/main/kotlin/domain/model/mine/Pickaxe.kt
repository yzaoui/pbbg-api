package com.bitwiserain.pbbg.domain.model.mine

import com.bitwiserain.pbbg.domain.model.Item
import com.bitwiserain.pbbg.domain.model.Point

/**
 * Entity that can be equipped in the pickaxe slot and used for mining.
 *
 * @property type The name of this pickaxe type.
 * @property cells The set cells that this pickaxe can reach, relative to its center at [0, 0].
 */
enum class Pickaxe(val type: String, val cells: Set<Point>) {
    PLUS("Plus-shaped", arrayOf(
        arrayOf(0, 1, 0),
        arrayOf(1, 1, 1),
        arrayOf(0, 1, 0)
    ).toPoints(3, 3, 1 to 1)),
    CROSS("Cross-shaped", arrayOf(
        arrayOf(1, 0, 1),
        arrayOf(0, 1, 0),
        arrayOf(1, 0, 1)
    ).toPoints(3, 3, 1 to 1)),
    SQUARE("Square-shaped", arrayOf(
        arrayOf(1, 1, 1),
        arrayOf(1, 0, 1),
        arrayOf(1, 1, 1)
    ).toPoints(3, 3, 1 to 1));

    // TODO: Not sure if this belongs here
    fun toItem(equipped: Boolean): Item.Pickaxe = when(this) {
        Pickaxe.PLUS -> Item.Pickaxe.PlusPickaxe(equipped)
        Pickaxe.CROSS -> Item.Pickaxe.CrossPickaxe(equipped)
        Pickaxe.SQUARE -> Item.Pickaxe.SquarePickaxe(equipped)
    }

    companion object {
        fun fromItem(item: Item.Pickaxe): Pickaxe = when(item) {
            is Item.Pickaxe.PlusPickaxe -> Pickaxe.PLUS
            is Item.Pickaxe.CrossPickaxe -> Pickaxe.CROSS
            is Item.Pickaxe.SquarePickaxe -> Pickaxe.SQUARE
        }
    }
}

private fun Array<Array<Int>>.toPoints(width: Int, height: Int, center: Point): Set<Point> {
    val set = mutableSetOf<Point>()

    for (row in 0 until height) {
        for (col in 0 until width) {
            if (this[row][col] == 1) set.add(col - center.x to row - center.y)
        }
    }

    return set
}

private infix fun Int.to(that: Int) = Point(this, that)
