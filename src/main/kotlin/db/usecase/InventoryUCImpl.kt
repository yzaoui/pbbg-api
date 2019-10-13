package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.Joins
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.model.Inventory
import com.bitwiserain.pbbg.domain.model.MaterializedItem
import com.bitwiserain.pbbg.domain.usecase.InventoryUC
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class InventoryUCImpl(private val db: Database) : InventoryUC {
    override fun getInventory(userId: Int): Inventory = transaction(db) {
        // TODO: Consider checking if user exists
        val userId = EntityID(userId, UserTable)

        val items = Joins.getInventoryItems(userId)

        Inventory(items)
    }

    override fun storeInInventory(userId: Int, itemToStore: MaterializedItem): Unit = transaction(db) {
        // TODO: Consider checking if user exists
        val userId = EntityID(userId, UserTable)

        storeInInventoryReturnItemID(db, userId, itemToStore)
    }
}
