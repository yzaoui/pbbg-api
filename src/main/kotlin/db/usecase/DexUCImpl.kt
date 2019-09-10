package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.DexTable
import com.bitwiserain.pbbg.db.repository.UserTable
import com.bitwiserain.pbbg.domain.model.BaseItem
import com.bitwiserain.pbbg.domain.model.ItemEnum
import com.bitwiserain.pbbg.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.domain.model.dex.DexItems
import com.bitwiserain.pbbg.domain.model.dex.DexUnits
import com.bitwiserain.pbbg.domain.usecase.DexUC
import com.bitwiserain.pbbg.domain.usecase.InvalidItemException
import com.bitwiserain.pbbg.domain.usecase.InvalidUnitException
import com.bitwiserain.pbbg.domain.usecase.ItemUndiscoveredException
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class DexUCImpl(private val db: Database) : DexUC {
    override fun getDexItems(userId: Int): DexItems = transaction(db) {
        val discoveredItems = DexTable.getDiscovered(EntityID(userId, UserTable))

        return@transaction DexItems(
            discoveredItems = discoveredItems,
            lastItemIsDiscovered = discoveredItems.contains(ItemEnum.values().last())
        )
    }

    override fun getDexItem(userId: Int, itemEnumId: Int): BaseItem = transaction(db) {
        if (itemEnumId !in ItemEnum.values().indices) throw InvalidItemException()

        val enum = ItemEnum.values()[itemEnumId]

        if (!DexTable.hasEntry(EntityID(userId, UserTable), enum)) throw ItemUndiscoveredException()

        return@transaction enum.baseItem
    }

    override fun getDexUnits(userId: Int): DexUnits = transaction(db ) {
        // TODO: Implement this
        val discoveredUnits = MyUnitEnum.values().toSet()

        return@transaction DexUnits(
            discoveredUnits = discoveredUnits,
            lastUnitIsDiscovered = true
        )
    }

    override fun getDexUnit(userId: Int, unitEnumId: Int): MyUnitEnum {
        // TODO: Make sure user can see this unit
        return try {
            MyUnitEnum.values()[unitEnumId]
        } catch (e: IndexOutOfBoundsException) {
            throw InvalidUnitException()
        }
    }
}
