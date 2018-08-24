package miner.domain.usecase

import data.model.Pickaxe
import miner.data.EquipmentTable
import miner.data.UserTable
import miner.route.api.toItem
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

interface EquipmentUC {
    fun getPickaxe(userId: Int): Pickaxe?
    fun getAllPickaxes(): Array<Pickaxe>
    fun generatePickaxe(userId: Int): Pickaxe?
}

class EquipmentUCImpl(private val inventoryUC: InventoryUC) : EquipmentUC {
    override fun getPickaxe(userId: Int): Pickaxe? = transaction {
        EquipmentTable.select { EquipmentTable.userId.eq(userId) }
            .map { it[EquipmentTable.pickaxe] }
            .singleOrNull()
    }

    override fun getAllPickaxes(): Array<Pickaxe> {
        return Pickaxe.values()
    }

    override fun generatePickaxe(userId: Int): Pickaxe? = transaction {
        // TODO: Do something if this user already has a pickaxe
        val pickaxe = Pickaxe.values()[Random().nextInt(Pickaxe.values().size)]

        inventoryUC.storeInInventory(userId, pickaxe.toItem(), 1)

        EquipmentTable.insert {
            it[EquipmentTable.userId] = EntityID(userId, UserTable)
            it[EquipmentTable.pickaxe] = pickaxe
        }

        pickaxe
    }
}
