package pbbg.data

import pbbg.data.model.MineEntity
import org.jetbrains.exposed.dao.IntIdTable

object MineContentsTable : IntIdTable() {
    val mineId = reference("mine_id", MineSessionTable)
    val x = integer("x")
    val y = integer("y")
    val mineItem = enumeration("mine_item", MineEntity::class.java)
}
