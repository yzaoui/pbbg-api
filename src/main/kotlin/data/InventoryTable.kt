package pbbg.data

import pbbg.data.model.Item
import org.jetbrains.exposed.sql.Table

object InventoryTable : Table() {
    val userId = reference("user_id", UserTable)
    val item = enumeration("item", Item::class.java)
    val quantity = integer("quantity")
}
