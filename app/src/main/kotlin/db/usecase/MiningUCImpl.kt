package com.bitwiserain.pbbg.app.db.usecase

import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.DexTable
import com.bitwiserain.pbbg.app.db.repository.InventoryTable
import com.bitwiserain.pbbg.app.db.repository.ItemHistoryTable
import com.bitwiserain.pbbg.app.db.repository.MaterializedItemTable
import com.bitwiserain.pbbg.app.db.repository.UserStatsTable
import com.bitwiserain.pbbg.app.db.repository.mine.MineCellTable
import com.bitwiserain.pbbg.app.db.repository.mine.MineSessionTable
import com.bitwiserain.pbbg.app.domain.MiningExperienceManager
import com.bitwiserain.pbbg.app.domain.model.BaseItem
import com.bitwiserain.pbbg.app.domain.model.InventoryItem
import com.bitwiserain.pbbg.app.domain.model.MaterializedItem
import com.bitwiserain.pbbg.app.domain.model.MaterializedItem.Stackable
import com.bitwiserain.pbbg.app.domain.model.Point
import com.bitwiserain.pbbg.app.domain.model.itemdetails.ItemHistoryInfo
import com.bitwiserain.pbbg.app.domain.model.mine.AvailableMines
import com.bitwiserain.pbbg.app.domain.model.mine.Mine
import com.bitwiserain.pbbg.app.domain.model.mine.MineActionResult
import com.bitwiserain.pbbg.app.domain.model.mine.MineEntity
import com.bitwiserain.pbbg.app.domain.model.mine.MineType
import com.bitwiserain.pbbg.app.domain.model.mine.MinedItemResult
import com.bitwiserain.pbbg.app.domain.usecase.AlreadyInMineException
import com.bitwiserain.pbbg.app.domain.usecase.InvalidMineTypeIdException
import com.bitwiserain.pbbg.app.domain.usecase.MiningUC
import com.bitwiserain.pbbg.app.domain.usecase.NoEquippedPickaxeException
import com.bitwiserain.pbbg.app.domain.usecase.NotInMineSessionException
import com.bitwiserain.pbbg.app.domain.usecase.UnfulfilledLevelRequirementException
import java.time.Clock
import kotlin.random.Random

class MiningUCImpl(
    private val transaction: Transaction,
    private val clock: Clock,
    private val dexTable: DexTable,
    private val inventoryTable: InventoryTable,
    private val itemHistoryTable: ItemHistoryTable,
    private val materializedItemTable: MaterializedItemTable,
    private val mineCellTable: MineCellTable,
    private val mineSessionTable: MineSessionTable,
    private val userStatsTable: UserStatsTable,
) : MiningUC {

    override fun getMine(userId: Int): Mine? = transaction {
        /* Get currently running mine session */
        val mineSession = mineSessionTable.getSession(userId) ?: return@transaction null

        val grid = mineCellTable.getGrid(mineSession.id)

        Mine(mineSession.width, mineSession.height, grid, mineSession.mineType)
    }

    override fun generateMine(userId: Int, mineTypeId: Int, width: Int, height: Int): Mine = transaction {
        // Don't generate mine when already in one
        if (mineSessionTable.getSession(userId) != null) throw AlreadyInMineException()

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

        val userMiningExp = userStatsTable.getUserStats(userId).miningExp

        val userMiningProgress = MiningExperienceManager.getLevelProgress(userMiningExp)

        if (userMiningProgress.level < mineType.minLevel) throw UnfulfilledLevelRequirementException(
            currentLevel = userMiningProgress.level,
            requiredMinimumLevel = mineType.minLevel
        )

        val mineSessionId = mineSessionTable.insertSessionAndGetId(userId, width, height, mineType)

        mineCellTable.insertCells(mineSessionId, itemEntries)

        return@transaction Mine(width, height, itemEntries, mineType)
    }

    override fun exitMine(userId: Int): Unit = transaction {
        mineSessionTable.deleteSession(userId)
    }

    override fun submitMineAction(userId: Int, x: Int, y: Int): MineActionResult = transaction {
        val now = clock.instant()

        /* Get currently running mine session */
        val mineSession = mineSessionTable.getSession(userId) ?: throw NotInMineSessionException()

        val inventoryItems = inventoryTable.getInventoryItems(userId)

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
        val reachableCellsWithContent = mineCellTable.getMineCells(mineSession.id)
            .filter { reacheableCells.contains(Point(it.x, it.y)) }

        // Mine entities with the quantity hit
        val mineEntitiesAndCount = reachableCellsWithContent.map { it.mineEntity }.groupingBy { it }.eachCount()

        val minedItemResults = mutableListOf<MinedItemResult>()
        var totalExp = 0
        for ((mineEntity, count) in mineEntitiesAndCount) {
            val item = mineEntityToItem(mineEntity, count)

            val exp = mineEntity.exp * (if (item is Stackable) item.quantity else 1)

            // TODO: Store items in batch
            val itemId = storeInInventoryReturnItemID(
                transaction, now, userId, item, ItemHistoryInfo.FirstMined(userId), dexTable, inventoryTable, itemHistoryTable, materializedItemTable
            )

            minedItemResults.add(MinedItemResult(itemId, item, mineEntity.exp))
            totalExp += exp
        }

        // Remove mined cells from database
        mineCellTable.deleteCells(reachableCellsWithContent.map { it.id })

        val userCurrentMiningExp = userStatsTable.getUserStats(userId).miningExp

        val currentLevelProgress = MiningExperienceManager.getLevelProgress(userCurrentMiningExp)
        val newLevelProgress = MiningExperienceManager.getLevelProgress(userCurrentMiningExp + totalExp)

        // Increase user's mining experience from this mining operation if progress was made
        if (currentLevelProgress != newLevelProgress) {
            userStatsTable.updateMiningExp(userId, newLevelProgress.absoluteExp)
        }

        MineActionResult(
            minedItemResults = minedItemResults,
            levelUps = MiningExperienceManager.getLevelUpResults(currentLevelProgress.level, newLevelProgress.level),
            mine = Mine(mineSession.width, mineSession.height, mineCellTable.getGrid(mineSession.id), mineSession.mineType),
            miningLvl = newLevelProgress
        )
    }

    override fun getAvailableMines(userId: Int): AvailableMines {
        val userMiningLevel = transaction {
            userStatsTable.getUserStats(userId).miningExp
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
