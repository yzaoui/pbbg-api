package com.bitwiserain.pbbg.app.domain.usecase

import com.bitwiserain.pbbg.app.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.app.domain.model.dex.DexItem
import com.bitwiserain.pbbg.app.domain.model.dex.DexItems
import com.bitwiserain.pbbg.app.domain.model.dex.DexPlants
import com.bitwiserain.pbbg.app.domain.model.dex.DexUnits
import com.bitwiserain.pbbg.app.domain.model.farm.BasePlant

interface DexUC {
    /**
     * The dex entries of items this user has discovered.
     */
    fun getDexItems(userId: Int): DexItems

    /**
     * Detailed entry of an item this user has discovered.
     *
     * @throws InvalidItemException when [itemId] is invalid.
     */
    fun getIndividualDexBaseItem(userId: Int, itemId: Int): DexItem

    /**
     * The dex entries of all units.
     */
    fun getDexUnits(userId: Int): DexUnits

    /**
     * Detailed entry of a unit.
     *
     * @throws InvalidUnitException when [unitId] is invalid.
     */
    fun getDexUnit(userId: Int, unitId: Int): MyUnitEnum

    /**
     * The dex entries of plants this user has harvested.
     */
    fun getDexPlants(userId: Int): DexPlants

    /**
     * Detailed entry of a plant.
     *
     * @throws InvalidPlantException when [plantId] is invalid.
     */
    fun getDexPlant(userId: Int, plantId: Int): BasePlant
}

class InvalidItemException : Exception()
class InvalidUnitException : Exception()
class InvalidPlantException : Exception()
