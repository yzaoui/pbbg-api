package com.bitwiserain.pbbg.app.domain.usecase.mine

import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.mine.MineSessionTable

/**
 * Exit the mine currently in session, if any.
 */
fun interface ExitMine : (Int) -> Unit {
    /**
     * @param userId The user's ID.
     */
    override fun invoke(userId: Int)
}

class ExitMineImpl(
    private val transaction: Transaction,
    private val mineSessionTable: MineSessionTable,
) : ExitMine {

    override fun invoke(userId: Int): Unit = transaction {
        mineSessionTable.deleteSession(userId)
    }
}
