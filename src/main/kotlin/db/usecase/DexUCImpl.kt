package com.bitwiserain.pbbg.db.usecase

import com.bitwiserain.pbbg.db.repository.DexTable
import com.bitwiserain.pbbg.domain.model.ItemEnum
import com.bitwiserain.pbbg.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.domain.model.dex.DexItem
import com.bitwiserain.pbbg.domain.model.dex.DexItems
import com.bitwiserain.pbbg.domain.model.dex.DexPlants
import com.bitwiserain.pbbg.domain.model.dex.DexUnits
import com.bitwiserain.pbbg.domain.model.farm.BasePlant
import com.bitwiserain.pbbg.domain.model.farm.PlantEnum
import com.bitwiserain.pbbg.domain.usecase.DexUC
import com.bitwiserain.pbbg.domain.usecase.InvalidItemException
import com.bitwiserain.pbbg.domain.usecase.InvalidPlantException
import com.bitwiserain.pbbg.domain.usecase.InvalidUnitException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class DexUCImpl(private val db: Database) : DexUC {
    override fun getDexItems(userId: Int): DexItems = transaction(db) {
        val discoveredItems = DexTable.getDiscovered(userId)

        return@transaction DexItems(
            discoveredItems = discoveredItems,
            lastItemId = ItemEnum.values().lastIndex + 1
        )
    }

    override fun getIndividualDexBaseItem(userId: Int, itemId: Int): DexItem = transaction(db) {
        val itemEnumOrdinal = itemId - 1
        if (itemEnumOrdinal !in ItemEnum.values().indices) throw InvalidItemException()

        val enum = ItemEnum.values()[itemEnumOrdinal]

        if (!DexTable.hasEntry(userId, enum)) return@transaction DexItem.UndiscoveredDexItem(itemId)

        return@transaction DexItem.DiscoveredDexItem(enum.baseItem)
    }

    override fun getDexUnits(userId: Int): DexUnits = transaction(db ) {
        val discoveredUnits = MyUnitEnum.values().toSet()

        return@transaction DexUnits(
            discoveredUnits = discoveredUnits,
            lastUnitId = MyUnitEnum.values().lastIndex + 1
        )
    }

    override fun getDexUnit(userId: Int, unitId: Int): MyUnitEnum {
        val unitEnumOrdinal = unitId - 1
        if (unitEnumOrdinal !in MyUnitEnum.values().indices) throw InvalidUnitException()

        return MyUnitEnum.values()[unitEnumOrdinal]
    }

    override fun getDexPlants(userId: Int): DexPlants {
        // TODO: Make sure user has discovered these plants
        return DexPlants(
            discoveredPlants = PlantEnum.values().associate { it.ordinal + 1 to it.basePlant },
            lastPlantId = PlantEnum.values().lastIndex + 1
        )
    }

    override fun getDexPlant(userId: Int, plantId: Int): BasePlant {
        val plantEnumOrdinal = plantId - 1
        if (plantEnumOrdinal !in PlantEnum.values().indices) throw InvalidPlantException()

        return PlantEnum.values()[plantEnumOrdinal].basePlant
    }
}
