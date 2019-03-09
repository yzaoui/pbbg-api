package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.DexTable
import com.bitwiserain.pbbg.db.repository.EquipmentTable
import com.bitwiserain.pbbg.db.repository.InventoryTable
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.model.*
import com.bitwiserain.pbbg.domain.model.ItemEnum.*
import com.bitwiserain.pbbg.domain.usecase.InventoryUC
import io.ktor.html.insert
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class InventoryUCImpl(private val db: Database) : InventoryUC {
    override fun getInventory(userId: Int): Inventory = transaction(db) {
        // TODO: Consider checking if user exists

        val items = InventoryTable.select { InventoryTable.userId.eq(userId) }
            .associate { it[InventoryTable.id].value to it.toItem() }

        val equippedPickaxe = EquipmentTable.select { EquipmentTable.userId.eq(userId) }
            .singleOrNull()
            ?.get(EquipmentTable.pickaxe)

        // TODO: Should have single source of truth, figure out where something exists as equipped
        Inventory(items, Equipment(equippedPickaxe?.toItem(equipped = true)))
    }

    override fun storeInInventory(userId: Int, itemToStore: Item): Unit = transaction(db) {
        // TODO: Consider checking if user exists
        val storedItem = InventoryTable.select { InventoryTable.userId.eq(userId) and InventoryTable.item.eq(itemToStore.enum) }
            .map { it.toItem() }
            .singleOrNull()

        if (storedItem != null && itemToStore is Stackable) {
            // If this kind of item is already stored, and it can be stacked, increase its quantity
            InventoryTable.update({ InventoryTable.userId.eq(userId) and InventoryTable.item.eq(itemToStore.enum) }) { updateStatement ->
                with (SqlExpressionBuilder) {
                    updateStatement.update(InventoryTable.quantity, InventoryTable.quantity + itemToStore.quantity)
                }
            }
        } else {
            // If this item isn't already stored, or it can't be stacked, create a new row for it
            InventoryTable.insert {
                it[InventoryTable.userId] = EntityID(userId, UserTable)
                it[InventoryTable.item] = itemToStore.enum
                if (itemToStore is Stackable) {
                    it[InventoryTable.quantity] = itemToStore.quantity
                }
                if (itemToStore is Equippable) {
                    it[InventoryTable.equipped] = itemToStore.equipped
                }
            }
        }

        /*************************************************
         * Update user's dex to check off this item type *
         *************************************************/
        if (!DexTable.hasEntry(userId, itemToStore.enum)) {
            DexTable.insert {
                it[DexTable.userId] = EntityID(userId, UserTable)
                it[DexTable.item] = itemToStore.enum
            }
        }
    }
}

fun ResultRow.toItem(): Item {
    val itemEnum = this[InventoryTable.item]
    val quantity = this[InventoryTable.quantity]
    val equipped = this[InventoryTable.equipped]

    // TODO: Find a way to preserve a single source of truth, so that quantity and equipped status aren't asserted separately here
    return when (itemEnum) {
        STONE -> Item.Material.Stone(quantity!!)
        COAL -> Item.Material.Coal(quantity!!)
        COPPER_ORE -> Item.Material.CopperOre(quantity!!)
        PLUS_PICKAXE -> Item.Pickaxe.PlusPickaxe(equipped!!)
        CROSS_PICKAXE -> Item.Pickaxe.CrossPickaxe(equipped!!)
        SQUARE_PICKAXE -> Item.Pickaxe.SquarePickaxe(equipped!!)
    }
}
