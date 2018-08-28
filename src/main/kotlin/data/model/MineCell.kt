package pbbg.data.model

/**
 * Represents the contents of a mine cell.
 */
data class MineCell(
    val id: Int,
    val x: Int,
    val y: Int,
    val mineEntity: MineEntity
)
