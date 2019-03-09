package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.DexTable
import com.bitwiserain.pbbg.domain.model.ItemEnum
import com.bitwiserain.pbbg.domain.model.dex.Dex
import com.bitwiserain.pbbg.domain.usecase.DexUC
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class DexUCImpl(private val db: Database) : DexUC {
    override fun getDex(userId: Int): Dex = transaction(db) {
        val discoveredItems = DexTable.select { DexTable.userId.eq(userId) }
            .map { it[DexTable.item] }
            .toSet()

        return@transaction Dex(
            discoveredItems = discoveredItems,
            lastItemIsDiscovered = discoveredItems.contains(ItemEnum.values().last())
        )
    }
}
