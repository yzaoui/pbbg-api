package com.bitwiserain.pbbg.app.domain.usecase.mine

import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.mine.MineCellTable
import com.bitwiserain.pbbg.app.db.repository.mine.MineSessionTable
import com.bitwiserain.pbbg.app.domain.model.mine.Mine

/**
 * Get the user's current mine, or `null` if not currently in one.
 */
fun interface GetMine : (Int) -> Mine? {
    /**
     * @param userId The user's ID.
     */
    override operator fun invoke(userId: Int): Mine?
}

class GetMineImpl(
    private val transaction: Transaction,
    private val mineCellTable: MineCellTable,
    private val mineSessionTable: MineSessionTable,
) : GetMine {

    override fun invoke(userId: Int): Mine? = transaction {
        /* Get currently running mine session */
        val mineSession = mineSessionTable.getSession(userId) ?: return@transaction null

        val grid = mineCellTable.getGrid(mineSession.id)

        Mine(mineSession.width, mineSession.height, grid, mineSession.mineType)
    }
}
