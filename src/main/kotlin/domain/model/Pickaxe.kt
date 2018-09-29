package com.bitwiserain.pbbg.domain.model

/**
 * Entity that can be equipped in the pickaxe slot and used for mining.
 *
 * @property type The name of this pickaxe type.
 * @property cells The set cells that this pickaxe can reach, relative to its center at [0, 0].
 */
enum class Pickaxe(val type: String, val cells: Set<Pair<Int, Int>>) {
    PLUS("Plus-shaped", setOf(
        0 to -1,
        -1 to 0,
        0 to 0,
        1 to 0,
        0 to 1
    )),
    CROSS("Cross-shaped", setOf(
        -1 to -1,
        1 to -1,
        0 to 0,
        -1 to 1,
        1 to 1
    )),
    SQUARE("Square-shaped", setOf(
        -1 to -1,
        0 to -1,
        1 to -1,
        -1 to 0,
        1 to 0,
        -1 to 1,
        0 to 1,
        1 to 1
    ));

    // TODO: Not sure if this belongs here
    fun toItem(equipped: Boolean): Item.Pickaxe = when(this) {
        Pickaxe.PLUS -> Item.Pickaxe.PlusPickaxe(equipped)
        Pickaxe.CROSS -> Item.Pickaxe.CrossPickaxe(equipped)
        Pickaxe.SQUARE -> Item.Pickaxe.SquarePickaxe(equipped)
    }
}
