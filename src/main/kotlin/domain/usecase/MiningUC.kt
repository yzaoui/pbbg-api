package miner.domain.usecase

import data.model.MineResultItem
import miner.data.EquipmentTable
import miner.data.MineContentsTable
import miner.data.MineSessionTable
import miner.data.UserTable
import miner.data.model.*
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.Random

interface MiningUC {
    fun getMine(userId: Int): Mine?
    fun generateMine(userId: Int, width: Int, height: Int): Mine
    fun mine(userId: Int, x: Int, y: Int): List<MineResultItem>?
}

class MiningUCImpl(private val db: Database, private val inventoryUC: InventoryUC) : MiningUC {
    private val random = Random()

    override fun getMine(userId: Int): Mine? = transaction(db) {
        val mineSession = MineSessionTable.select { MineSessionTable.userId.eq(userId) }
            .map { it.toMineSession() }
            .singleOrNull() ?: return@transaction null

        val grid = mutableMapOf<Pair<Int, Int>, MineEntity> ()
        MineContentsTable.select { MineContentsTable.mineId.eq(mineSession.id) }
            .forEach { grid[it[MineContentsTable.x] to it[MineContentsTable.y]] = it[MineContentsTable.mineItem] }

        Mine(mineSession.width, mineSession.height, grid)
    }

    override fun generateMine(userId: Int, width: Int, height: Int): Mine {
        val itemEntries = mutableMapOf<Pair<Int, Int>, MineEntity>()
        (0 until height).forEach { y ->
            (0 until width).forEach { x ->
                rollForRandomMineItem()?.let { itemEntries.put(x to y, it) }
            }
        }

        transaction(db) {
            val mineSessionId = MineSessionTable.insertAndGetId {
                it[MineSessionTable.userId] = EntityID(userId, UserTable)
                it[MineSessionTable.width] = width
                it[MineSessionTable.height] = height
            }

            MineContentsTable.batchInsert(itemEntries.asIterable()) { (pos, entity) ->
                this[MineContentsTable.mineId] = mineSessionId
                this[MineContentsTable.x] = pos.first
                this[MineContentsTable.y] = pos.second
                this[MineContentsTable.mineItem] = entity
            }
        }

        return Mine(width, height, itemEntries)
    }

    override fun mine(userId: Int, x: Int, y: Int): List<MineResultItem>? = transaction(db) {
        val pickaxe = EquipmentTable.select { EquipmentTable.userId.eq(userId) }
            .map { it[EquipmentTable.pickaxe] }
            .singleOrNull() ?: return@transaction null

        val mineSession = MineSessionTable.select { MineSessionTable.userId.eq(userId) }
            .map { it.toMineSession() }
            .singleOrNull() ?: return@transaction null

        val reacheableCells = reachableCells(x, y, mineSession.width, mineSession.height, pickaxe.cells)
        val cellsWithContent = MineContentsTable.select { MineContentsTable.mineId.eq(mineSession.id) }
            .map { it.toMineContent() }
        val reachableCellsWithContent = cellsWithContent.filter { reacheableCells.contains(it.x to it.y) }
        val obtainedItems = reachableCellsWithContent.map { it.mineEntity.toItem() }
        val obtainedItemsGroupWithCount = obtainedItems.groupingBy { it }.eachCount()

        MineContentsTable.deleteWhere { MineContentsTable.id.inList(reachableCellsWithContent.map { it.id }) }

        //TODO: Store in batch
        obtainedItemsGroupWithCount.forEach {
            inventoryUC.storeInInventory(userId, it.key, it.value)
        }

        obtainedItemsGroupWithCount.map { MineResultItem(it.key, it.value) }
    }

    private fun ResultRow.toMineSession() = MineSession(
        id = this[MineSessionTable.id].value,
        width = this[MineSessionTable.width],
        height = this[MineSessionTable.height]
    )

    private fun ResultRow.toMineContent() = MineCell(
        id = this[MineContentsTable.id].value,
        x = this[MineContentsTable.x],
        y = this[MineContentsTable.y],
        mineEntity = this[MineContentsTable.mineItem]
    )

    private fun rollForRandomMineItem(): MineEntity? {
        val roll = random.nextFloat()
        return when {
            roll <= 0.05 -> MineEntity.ROCK
            else -> null
        }
    }

    private fun MineEntity.toItem(): Item {
        return when (this) {
            MineEntity.ROCK -> Item.STONE
        }
    }

    private fun reachableCells(x: Int, y: Int, width: Int, height: Int, tiles: Set<Pair<Int, Int>>): Set<Pair<Int, Int>> {
        val cells = mutableSetOf<Pair<Int, Int>>()

        tiles.forEach {
            val newX = x + it.first
            val newY = y + it.second

            if ((newX in 0 until width) && newY in 0 until height) {
                cells.add(newX to newY)
            }
        }

        return cells
    }
}
