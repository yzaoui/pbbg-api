package pbbg.data

import org.jetbrains.exposed.dao.IntIdTable

object MineSessionTable : IntIdTable() {
    val userId = reference("user_id", UserTable).uniqueIndex()
    val width = integer("width")
    val height = integer("height")
}
