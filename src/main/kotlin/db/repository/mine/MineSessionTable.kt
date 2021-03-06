package com.bitwiserain.pbbg.db.repository.mine

import com.bitwiserain.pbbg.db.model.MineSession
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.model.mine.MineType
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select

object MineSessionTable : IntIdTable() {
    val userId = reference("user_id", UserTable).uniqueIndex()
    val width = integer("width")
    val height = integer("height")
    val mineType = enumeration("mine_type", MineType::class)

    fun insertSessionAndGetId(userId: Int, width: Int, height: Int, mineType: MineType) = insertAndGetId {
        it[MineSessionTable.userId] = EntityID(userId, UserTable)
        it[MineSessionTable.width] = width
        it[MineSessionTable.height] = height
        it[MineSessionTable.mineType] = mineType
    }

    fun getSession(userId: Int) = select { MineSessionTable.userId.eq(userId) }
        .map { it.toMineSession() }
        .singleOrNull()

    fun deleteSession(userId: Int) = deleteWhere {
        MineSessionTable.userId.eq(userId)
    }

    private fun ResultRow.toMineSession() = MineSession(
        id = this[id].value,
        width = this[width],
        height = this[height],
        mineType = this[mineType]
    )
}
