package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.model.MineCell
import com.bitwiserain.pbbg.db.repository.Joins
import com.bitwiserain.pbbg.db.repository.UserStatsTable
import com.bitwiserain.pbbg.db.repository.mine.MineCellTable
import com.bitwiserain.pbbg.db.repository.mine.MineSessionTable
import com.bitwiserain.pbbg.domain.MiningExperienceManager
import com.bitwiserain.pbbg.domain.model.BaseItem
import com.bitwiserain.pbbg.domain.model.InventoryItem
import com.bitwiserain.pbbg.domain.model.MaterializedItem
import com.bitwiserain.pbbg.domain.model.MaterializedItem.Stackable
import com.bitwiserain.pbbg.domain.model.Point
import com.bitwiserain.pbbg.domain.model.itemdetails.ItemHistoryInfo
import com.bitwiserain.pbbg.domain.model.mine.*
import com.bitwiserain.pbbg.domain.usecase.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Clock
import kotlin.random.Random

class MiningUCImpl(private val db: Database, private val clock: Clock) : MiningUC {
    override fun getMine(userId: Int): Mine? = transaction(db) {
        /* Get currently running mine session */
        val mineSession = MineSessionTable.getSession(userId) ?: return@transaction null

        val grid = MineCellTable.getGrid(mineSession.id)

        Mine(mineSession.width, mineSession.height, grid, mineSession.mineType)
    }

    override fun generateMine(userId: Int, mineTypeId: Int, width: Int, height: Int): Mine {
        // Don't generate mine when already in one
        if (getMine(userId) != null) throw AlreadyInMineException()

        val mineType = try {
            MineType.values()[mineTypeId]
        } catch (e: ArrayIndexOutOfBoundsException) {
            throw InvalidMineTypeIdException(id = mineTypeId)
        }

        val itemEntries = mutableMapOf<Pair<Int, Int>, MineEntity>()
        (0 until height).forEach { y ->
            (0 until width).forEach { x ->
                mineType.rollForMineEntity(Random.nextFloat())?.let {
                    itemEntries.put(x to y, it)
                }
            }
        }

        transaction(db) {
            val userMiningExp = UserStatsTable.select { UserStatsTable.userId.eq(userId) }
                .single()
                .get(UserStatsTable.miningExp)

            val userMiningProgress = MiningExperienceManager.getLevelProgress(userMiningExp)

            if (userMiningProgress.level < mineType.minLevel) throw UnfulfilledLevelRequirementException(
                currentLevel = userMiningProgress.level,
                requiredMinimumLevel = mineType.minLevel
            )

            val mineSessionId = MineSessionTable.insertSessionAndGetId(userId, width, height, mineType)

            MineCellTable.batchInsert(itemEntries.asIterable()) { (pos, entity) ->
                this[MineCellTable.mineId] = mineSessionId
                this[MineCellTable.x] = pos.first
                this[MineCellTable.y] = pos.second
                this[MineCellTable.mineEntity] = entity
            }
        }

        return Mine(width, height, itemEntries, mineType)
    }

    override fun exitMine(userId: Int): Unit = transaction(db) {
        MineSessionTable.deleteSession(userId)
    }

    override fun submitMineAction(userId: Int, x: Int, y: Int): MineActionResult = transaction(db) {
        val now = clock.instant()

        /* Get currently running mine session */
        val mineSession = MineSessionTable.getSession(userId) ?: throw NotInMineSessionException()

        val inventoryItems = Joins.getInventoryItems(userId)

        /* Get currently equipped pickaxe */
        val pickaxe = inventoryItems
            .filter {
                val invItem = it.value
                return@filter invItem is InventoryItem.Equippable && invItem.equipped && it.value.base is BaseItem.Pickaxe
            }
            .entries.singleOrNull()
            ?.let { it.value.item }
            ?: throw NoEquippedPickaxeException()
        val pickaxeBase = pickaxe.base as? BaseItem.Pickaxe
        if (pickaxeBase !is BaseItem.Pickaxe) throw NoEquippedPickaxeException()

        // Cells that the currently equipped pickaxe at this location can reach
        val reacheableCells = reachableCells(x, y, mineSession.width, mineSession.height, pickaxeBase.grid)

        // The mine cells of this mine, filtered to only get those that are reachable with this pickaxe and location
        // TODO: Exposed isn't likely to support tuples in `WHERE IN` expressions, consider using raw SQL
        val reachableCellsWithContent = MineCellTable.select { MineCellTable.mineId.eq(mineSession.id) }
            .map { it.toMineCell() }
            .filter { reacheableCells.contains(Point(it.x, it.y)) }

        // Mine entities with the quantity hit
        val mineEntitiesAndCount = reachableCellsWithContent.map { it.mineEntity }.groupingBy { it }.eachCount()

        val minedItemResults = mutableListOf<MinedItemResult>()
        var totalExp = 0
        for ((mineEntity, count) in mineEntitiesAndCount) {
            val item = mineEntityToItem(mineEntity, count)

            val exp = mineEntity.exp * (if (item is Stackable) item.quantity else 1)

            // TODO: Store items in batch
            val itemId = storeInInventoryReturnItemID(db, now, userId, item, ItemHistoryInfo.FirstMined(userId))

            minedItemResults.add(MinedItemResult(itemId, item, mineEntity.exp))
            totalExp += exp
        }

        // Remove mined cells from database
        MineCellTable.deleteWhere { MineCellTable.id.inList(reachableCellsWithContent.map { it.id }) }

        val userCurrentMiningExp = UserStatsTable.getUserStats(userId).miningExp

        val currentLevelProgress = MiningExperienceManager.getLevelProgress(userCurrentMiningExp)
        val newLevelProgress = MiningExperienceManager.getLevelProgress(userCurrentMiningExp + totalExp)

        // Increase user's mining experience from this mining operation if progress was made
        if (currentLevelProgress != newLevelProgress) {
            UserStatsTable.updateMiningExp(userId, newLevelProgress.absoluteExp)
        }

        MineActionResult(
            minedItemResults = minedItemResults,
            levelUps = MiningExperienceManager.getLevelUpResults(currentLevelProgress.level, newLevelProgress.level),
            mine = Mine(mineSession.width, mineSession.height, MineCellTable.getGrid(mineSession.id), mineSession.mineType),
            miningLvl = newLevelProgress
        )
    }

    override fun getAvailableMines(userId: Int): AvailableMines {
        val userMiningLevel = transaction(db) {
            UserStatsTable.select { UserStatsTable.userId.eq(userId) }
                .single()
                .get(UserStatsTable.miningExp)
        }.let { exp ->
            MiningExperienceManager.getLevelProgress(exp)
        }.level

        val mines = mutableListOf<MineType>()
        var nextUnlockLevel: Int? = null

        for (mine in MineType.values()) {
            if (userMiningLevel >= mine.minLevel) {
                mines.add(mine)
            } else {
                nextUnlockLevel = mine.minLevel
                break
            }
        }

        return AvailableMines(mines, nextUnlockLevel)
    }

    private fun mineEntityToItem(entity: MineEntity, quantity: Int): MaterializedItem = when (entity) {
        MineEntity.ROCK -> MaterializedItem.Stone(quantity)
        MineEntity.COAL -> MaterializedItem.Coal(quantity)
        MineEntity.COPPER -> MaterializedItem.CopperOre(quantity)
    }

    private fun ResultRow.toMineCell() = MineCell(
        id = this[MineCellTable.id].value,
        x = this[MineCellTable.x],
        y = this[MineCellTable.y],
        mineEntity = this[MineCellTable.mineEntity]
    )

    private fun reachableCells(x: Int, y: Int, width: Int, height: Int, tiles: Set<Point>): Set<Point> {
        val cells = mutableSetOf<Point>()

        tiles.forEach {
            val newX = x + it.x
            val newY = y + it.y

            if ((newX in 0 until width) && newY in 0 until height) {
                cells.add(Point(newX, newY))
            }
        }

        return cells
    }
}
