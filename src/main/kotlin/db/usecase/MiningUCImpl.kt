package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.model.MineCell
import com.bitwiserain.pbbg.db.model.MineSession
import com.bitwiserain.pbbg.db.repository.EquipmentTable
import com.bitwiserain.pbbg.db.repository.MineCellTable
import com.bitwiserain.pbbg.db.repository.MineSessionTable
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.model.Item
import com.bitwiserain.pbbg.domain.model.mine.Mine
import com.bitwiserain.pbbg.domain.model.mine.MineEntity
import com.bitwiserain.pbbg.domain.usecase.*
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.Random

class MiningUCImpl(private val db: Database, private val inventoryUC: InventoryUC) : MiningUC {
    private val random = Random()

    override fun getMine(userId: Int): Mine? = transaction(db) {
        val mineSession = MineSessionTable.select { MineSessionTable.userId.eq(userId) }
            .map { it.toMineSession() }
            .singleOrNull() ?: return@transaction null

        val grid = mutableMapOf<Pair<Int, Int>, MineEntity> ()
        MineCellTable.select { MineCellTable.mineId.eq(mineSession.id) }
            .forEach { grid[it[MineCellTable.x] to it[MineCellTable.y]] = it[MineCellTable.mineEntity] }

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

            MineCellTable.batchInsert(itemEntries.asIterable()) { (pos, entity) ->
                this[MineCellTable.mineId] = mineSessionId
                this[MineCellTable.x] = pos.first
                this[MineCellTable.y] = pos.second
                this[MineCellTable.mineEntity] = entity
            }
        }

        return Mine(width, height, itemEntries)
    }

    override fun submitMineAction(userId: Int, x: Int, y: Int): List<Item> = transaction(db) {
        val pickaxe = EquipmentTable.select { EquipmentTable.userId.eq(userId) }
            .map { it[EquipmentTable.pickaxe] }
            .singleOrNull() ?: throw NoEquippedPickaxeException()

        val mineSession = MineSessionTable.select { MineSessionTable.userId.eq(userId) }
            .map { it.toMineSession() }
            .singleOrNull() ?: throw NotInMineSessionException()

        val reacheableCells = reachableCells(x, y, mineSession.width, mineSession.height, pickaxe.cells)
        val cellsWithContent = MineCellTable.select { MineCellTable.mineId.eq(mineSession.id) }
            .map { it.toMineContent() }
        val reachableCellsWithContent = cellsWithContent.filter { reacheableCells.contains(it.x to it.y) }
        val mappedMineEntitiesToCount = reachableCellsWithContent.map { it.mineEntity }.groupingBy { it }.eachCount()
        val obtainedItems = mappedMineEntitiesToCount.flatMap { mineEntityToItem(it.key, it.value) }

        MineCellTable.deleteWhere { MineCellTable.id.inList(reachableCellsWithContent.map { it.id }) }

        //TODO: Store in batch
        obtainedItems.forEach {
            inventoryUC.storeInInventory(userId, it)
        }

        obtainedItems
    }

    private fun mineEntityToItem(entity: MineEntity, quantity: Int): List<Item> = when (entity) {
        MineEntity.ROCK -> listOf(Item.Material.Stone(quantity))
        MineEntity.COAL -> listOf(Item.Material.Coal(quantity))
    }

    private fun ResultRow.toMineSession() = MineSession(
        id = this[MineSessionTable.id].value,
        width = this[MineSessionTable.width],
        height = this[MineSessionTable.height]
    )

    private fun ResultRow.toMineContent() = MineCell(
        id = this[MineCellTable.id].value,
        x = this[MineCellTable.x],
        y = this[MineCellTable.y],
        mineEntity = this[MineCellTable.mineEntity]
    )

    private fun rollForRandomMineItem(): MineEntity? {
        val roll = random.nextFloat()
        return when {
            roll <= 0.05 -> MineEntity.ROCK
            roll <= 0.06 -> MineEntity.COAL
            else -> null
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
