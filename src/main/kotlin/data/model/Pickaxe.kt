package data.model

/**
 * Entity that can be equipped in the pickaxe slot and used for mining.
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
    RECTANGLE("Open rectangle", setOf(
        -1 to -1,
        0 to -1,
        1 to -1,
        -1 to 0,
        1 to 0,
        -1 to 1,
        0 to 1,
        1 to 1
    ))
}
