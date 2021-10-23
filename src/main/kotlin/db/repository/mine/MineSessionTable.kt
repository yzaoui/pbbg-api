package com.bitwiserain.pbbg.db.repository.mine

import com.bitwiserain.pbbg.db.model.MineSession
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.model.mine.MineType
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select

interface MineSessionTable {

    fun insertSessionAndGetId(userId: Int, width: Int, height: Int, mineType: MineType): Int

    fun getSession(userId: Int): MineSession?

    fun deleteSession(userId: Int)
}

class MineSessionTableImpl : MineSessionTable {

    object Exposed : IntIdTable(name = "MineSession") {

        val userId = reference("user_id", UserTable).uniqueIndex()
        val width = integer("width")
        val height = integer("height")
        val mineType = enumeration("mine_type", MineType::class)
    }

    override fun insertSessionAndGetId(userId: Int, width: Int, height: Int, mineType: MineType): Int = Exposed.insertAndGetId {
        it[Exposed.userId] = EntityID(userId, UserTable)
        it[Exposed.width] = width
        it[Exposed.height] = height
        it[Exposed.mineType] = mineType
    }.value

    override fun getSession(userId: Int) = Exposed
        .select { Exposed.userId.eq(userId) }
        .map {
            MineSession(
                id = it[Exposed.id].value,
                width = it[Exposed.width],
                height = it[Exposed.height],
                mineType = it[Exposed.mineType]
            )
        }
        .singleOrNull()

    override fun deleteSession(userId: Int) {
        Exposed.deleteWhere { Exposed.userId.eq(userId) }
    }
}
