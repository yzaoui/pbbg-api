package com.bitwiserain.pbbg.app.db.repository.mine

import com.bitwiserain.pbbg.app.db.model.MineCell
import com.bitwiserain.pbbg.app.domain.model.mine.MineEntity
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select

interface MineCellTable {

    fun getGrid(mineSessionId: Int): Map<Pair<Int, Int>, MineEntity>

    fun getMineCells(mineSessionId: Int): List<MineCell>

    fun insertCells(mineSessionId: Int, cells: Map<Pair<Int, Int>, MineEntity>)

    fun deleteCells(cellIds: List<Int>)
}

class MineCellTableImpl : MineCellTable {

    object Exposed : IntIdTable(name = "MineCell") {

        val mineId = reference("mine_id", MineSessionTableImpl.Exposed, ReferenceOption.CASCADE)
        val x = integer("x")
        val y = integer("y")
        val mineEntity = enumeration("mine_entity", MineEntity::class)
    }

    override fun getGrid(mineSessionId: Int): Map<Pair<Int, Int>, MineEntity> {
        val grid = mutableMapOf<Pair<Int, Int>, MineEntity> ()

        Exposed.select { Exposed.mineId.eq(mineSessionId) }
            .forEach { grid[it[Exposed.x] to it[Exposed.y]] = it[Exposed.mineEntity] }

        return grid
    }

    override fun getMineCells(mineSessionId: Int): List<MineCell> = Exposed
        .select { Exposed.mineId.eq(mineSessionId) }
            .map {
                MineCell(
                    id = it[Exposed.id].value,
                    x = it[Exposed.x],
                    y = it[Exposed.y],
                    mineEntity = it[Exposed.mineEntity]
                )
            }

    override fun insertCells(mineSessionId: Int, cells: Map<Pair<Int, Int>, MineEntity>) {
        Exposed.batchInsert(cells.asIterable()) { (pos, entity) ->
            this[Exposed.mineId] = mineSessionId
            this[Exposed.x] = pos.first
            this[Exposed.y] = pos.second
            this[Exposed.mineEntity] = entity
        }
    }

    override fun deleteCells(cellIds: List<Int>) {
        Exposed.deleteWhere { Exposed.id.inList(cellIds) }
    }
}
