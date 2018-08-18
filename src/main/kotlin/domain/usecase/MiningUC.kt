package miner.domain.usecase

import miner.data.MineContentsTable
import miner.data.MineSessionTable
import miner.data.UserTable
import miner.data.model.Mine
import miner.data.model.MineItem
import miner.data.model.MineSession
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

interface MiningUC {
    fun getMineSession(userId: Int): Int?
    fun getMine(mineSessionId: Int): Mine?
    fun generateMine(userId: Int, width: Int, height: Int)
}

class MiningUCImpl : MiningUC {
    private val random = Random()

    override fun getMineSession(userId: Int): Int? = transaction {
        MineSessionTable.select { MineSessionTable.userId.eq(userId) }
            .map { it[MineSessionTable.id].value }
            .singleOrNull()
    }

    override fun getMine(mineSessionId: Int): Mine? = transaction {
        val mineSession = MineSessionTable.select { MineSessionTable.id.eq(mineSessionId) }
            .map { it.toMineSession() }
            .singleOrNull() ?: return@transaction null

        val grid = mutableMapOf<Pair<Int, Int>, MineItem> ()
        MineContentsTable.select { MineContentsTable.mineId.eq(mineSessionId) }
            .forEach { grid[it[MineContentsTable.x] to it[MineContentsTable.y]] = it[MineContentsTable.content] }

        Mine(mineSession.width, mineSession.height, grid)
    }

    override fun generateMine(userId: Int, width: Int, height: Int) {
        val itemEntries = mutableSetOf<Triple<Int, Int, MineItem>>()
        (0 until height).forEach { y ->
            (0 until width).forEach { x ->
                rollForRandomMineItem()?.let { itemEntries.add(Triple(x, y, it)) }
            }
        }

        transaction {
            val mineSessionId = MineSessionTable.insertAndGetId {
                it[MineSessionTable.userId] = EntityID(userId, UserTable)
                it[MineSessionTable.width] = width
                it[MineSessionTable.height] = height
            }

            MineContentsTable.batchInsert(itemEntries) { item ->
                this[MineContentsTable.mineId] = mineSessionId
                this[MineContentsTable.x] = item.first
                this[MineContentsTable.y] = item.second
                this[MineContentsTable.content] = item.third
            }
        }
    }

    private fun ResultRow.toMineSession() = MineSession(
        id = this[MineSessionTable.id].value,
        width = this[MineSessionTable.width],
        height = this[MineSessionTable.height]
    )

    private fun rollForRandomMineItem(): MineItem? {
        val roll = random.nextFloat()
        return when {
            roll <= 0.05 -> MineItem.ROCK
            else -> null
        }
    }
}


