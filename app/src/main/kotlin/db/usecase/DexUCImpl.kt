package com.bitwiserain.pbbg.app.db.usecase

import com.bitwiserain.pbbg.app.db.Transaction
import com.bitwiserain.pbbg.app.db.repository.DexTable
import com.bitwiserain.pbbg.app.domain.model.ItemEnum
import com.bitwiserain.pbbg.app.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.app.domain.model.dex.DexItem
import com.bitwiserain.pbbg.app.domain.model.dex.DexItems
import com.bitwiserain.pbbg.app.domain.model.dex.DexPlants
import com.bitwiserain.pbbg.app.domain.model.dex.DexUnits
import com.bitwiserain.pbbg.app.domain.model.farm.BasePlant
import com.bitwiserain.pbbg.app.domain.model.farm.PlantEnum
import com.bitwiserain.pbbg.app.domain.usecase.DexUC
import com.bitwiserain.pbbg.app.domain.usecase.InvalidItemException
import com.bitwiserain.pbbg.app.domain.usecase.InvalidPlantException
import com.bitwiserain.pbbg.app.domain.usecase.InvalidUnitException

class DexUCImpl(private val transaction: Transaction, private val dexTable: DexTable) : DexUC {
    override fun getDexItems(userId: Int): DexItems = transaction {
        val discoveredItems = dexTable.getDiscovered(userId)

        return@transaction DexItems(
            discoveredItems = discoveredItems,
            lastItemId = ItemEnum.values().lastIndex + 1
        )
    }

    override fun getIndividualDexBaseItem(userId: Int, itemId: Int): DexItem = transaction {
        val itemEnumOrdinal = itemId - 1
        if (itemEnumOrdinal !in ItemEnum.values().indices) throw InvalidItemException()

        val enum = ItemEnum.values()[itemEnumOrdinal]

        if (!dexTable.hasEntry(userId, enum)) return@transaction DexItem.UndiscoveredDexItem(itemId)

        return@transaction DexItem.DiscoveredDexItem(enum.baseItem)
    }

    override fun getDexUnits(userId: Int): DexUnits = transaction {
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
