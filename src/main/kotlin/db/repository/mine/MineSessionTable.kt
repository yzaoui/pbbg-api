package com.bitwiserain.pbbg.db.repository.mine

import com.bitwiserain.pbbg.db.model.MineSession
import com.bitwiserain.pbbg.db.repository.UserTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select

object MineSessionTable : IntIdTable() {
    val userId = reference("user_id", UserTable).uniqueIndex()
    val width = integer("width")
    val height = integer("height")

    fun insertSessionAndGetId(userId: EntityID<Int>, width: Int, height: Int) = insertAndGetId {
        it[MineSessionTable.userId] = userId
        it[MineSessionTable.width] = width
        it[MineSessionTable.height] = height
    }

    fun getSession(userId: EntityID<Int>) = select { MineSessionTable.userId.eq(userId) }
        .map { it.toMineSession() }
        .singleOrNull()

    fun deleteSession(userId: EntityID<Int>) = deleteWhere {
        MineSessionTable.userId.eq(userId)
    }

    private fun ResultRow.toMineSession() = MineSession(
        id = this[id].value,
        width = this[width],
        height = this[height]
    )
}
