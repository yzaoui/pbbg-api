package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.EquipmentTable
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.model.Item
import com.bitwiserain.pbbg.domain.model.Pickaxe
import com.bitwiserain.pbbg.domain.usecase.EquipmentUC
import com.bitwiserain.pbbg.domain.usecase.InventoryUC
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class EquipmentUCImpl(private val db: Database, private val inventoryUC: InventoryUC) : EquipmentUC {
    override fun getEquippedPickaxe(userId: Int): Pickaxe? = transaction(db) {
        EquipmentTable.select { EquipmentTable.userId.eq(userId) }
            .map { it[EquipmentTable.pickaxe] }
            .singleOrNull()
    }

    override fun generatePickaxe(userId: Int): Item.Pickaxe? = transaction(db) {
        // TODO: Do something if this user already has a pickaxe
        val pickaxeEnum = Pickaxe.values()[Random().nextInt(Pickaxe.values().size)]
        val pickaxeItem = pickaxeEnum.toItem(equipped = true)

        inventoryUC.storeInInventory(userId, pickaxeItem)

        EquipmentTable.insert {
            it[EquipmentTable.userId] = EntityID(userId, UserTable)
            it[EquipmentTable.pickaxe] = pickaxeEnum
        }

        pickaxeItem
    }
}
