package com.bitwiserain.pbbg.db.repository.mine

import com.bitwiserain.pbbg.domain.model.mine.MineEntity
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.select

object MineCellTable : IntIdTable() {
    val mineId = reference("mine_id", MineSessionTable, ReferenceOption.CASCADE)
    val x = integer("x")
    val y = integer("y")
    val mineEntity = enumeration("mine_entity", MineEntity::class)

    fun getGrid(mineSessionId: Int): Map<Pair<Int, Int>, MineEntity> {
        val grid = mutableMapOf<Pair<Int, Int>, MineEntity> ()

        MineCellTable.select { mineId.eq(mineSessionId) }
            .forEach { grid[it[x] to it[y]] = it[mineEntity] }

        return grid
    }
}
