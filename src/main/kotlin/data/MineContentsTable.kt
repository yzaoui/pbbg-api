package miner.data

import miner.data.model.MineItem
import org.jetbrains.exposed.sql.Table

object MineContentsTable : Table() {
    val mineId = reference("mine_id", MineSessionTable)
    val x = integer("x")
    val y = integer("y")
    val content = enumeration("content", MineItem::class.java)
}
