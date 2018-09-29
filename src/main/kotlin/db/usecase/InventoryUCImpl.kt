package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.EquipmentTable
import com.bitwiserain.pbbg.db.repository.InventoryTable
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.model.Equippable
import com.bitwiserain.pbbg.domain.model.Inventory
import com.bitwiserain.pbbg.domain.model.Item
import com.bitwiserain.pbbg.domain.model.ItemEnum.*
import com.bitwiserain.pbbg.domain.model.Stackable
import com.bitwiserain.pbbg.domain.usecase.InventoryUC
import domain.model.Equipment
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class InventoryUCImpl(private val db: Database) : InventoryUC {
    override fun getInventory(userId: Int): Inventory = transaction(db) {
        // TODO: Consider checking if user exists

        val items = InventoryTable.select { InventoryTable.userId.eq(userId) }
            .map { it.toItem() }

        val equippedPickaxe = EquipmentTable.select { EquipmentTable.userId.eq(userId) }
            .singleOrNull()
            ?.get(EquipmentTable.pickaxe)

        // TODO: Should have single source of truth, figure out where something exists as equipped
        Inventory(items, Equipment(equippedPickaxe?.toItem(equipped = true)))
    }

    override fun storeInInventory(userId: Int, item: Item): Unit = transaction(db) {
        // TODO: Consider checking if user exists
        val storedItem = InventoryTable.select { InventoryTable.userId.eq(userId) and InventoryTable.item.eq(item.enum) }
            .map { it.toItem() }
            .singleOrNull()

        if (storedItem != null) {
            // If this kind of item is already stored, and it can be stacked, increase its quantity
            if (item is Stackable) {
                InventoryTable.update({ InventoryTable.userId.eq(userId) and InventoryTable.item.eq(item.enum) }) { updateStatement ->
                    with (SqlExpressionBuilder) {
                        updateStatement.update(InventoryTable.quantity, InventoryTable.quantity + item.quantity)
                    }
                }
            }
        } else {
            // If this item isn't already stored, create an entry for it
            InventoryTable.insert {
                it[InventoryTable.userId] = EntityID(userId, UserTable)
                it[InventoryTable.item] = item.enum
                if (item is Stackable) {
                    it[InventoryTable.quantity] = item.quantity
                }
                if (item is Equippable) {
                    it[InventoryTable.equipped] = item.equipped
                }
            }
        }
    }

    private fun ResultRow.toItem(): Item {
        val itemEnum = this[InventoryTable.item]
        val quantity = this[InventoryTable.quantity]
        val equipped = this[InventoryTable.equipped]

        // TODO: Find a way to preserve a single source of truth, so that quantity and equipped status aren't asserted separately here
        return when (itemEnum) {
            STONE -> Item.Material.Stone(quantity!!)
            COAL -> Item.Material.Coal(quantity!!)
            PLUS_PICKAXE -> Item.Pickaxe.PlusPickaxe(equipped!!)
            CROSS_PICKAXE -> Item.Pickaxe.CrossPickaxe(equipped!!)
            SQUARE_PICKAXE -> Item.Pickaxe.SquarePickaxe(equipped!!)
        }
    }
}
