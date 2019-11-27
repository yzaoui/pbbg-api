package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.BaseItem
import com.bitwiserain.pbbg.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.domain.model.dex.DexItems
import com.bitwiserain.pbbg.domain.model.dex.DexUnits

interface DexUC {
    /**
     * The dex entries of items this user has discovered.
     */
    fun getDexItems(userId: Int): DexItems

    /**
     * Detailed entry of an item this user has discovered.
     *
     * @throws InvalidItemException when [itemEnumId] is invalid.
     * @throws ItemUndiscoveredException when this item has not been discovered by this user.
     */
    fun getIndividualDexBaseItem(userId: Int, itemEnumId: Int): BaseItem

    /**
     * The dex entries of all units.
     */
    fun getDexUnits(userId: Int): DexUnits

    /**
     * Detailed entry of a unit.
     *
     * @throws InvalidUnitException when [unitEnumId] is invalid.
     */
    fun getDexUnit(userId: Int, unitEnumId: Int): MyUnitEnum
}

class InvalidItemException : Exception()
class ItemUndiscoveredException : Exception()
class InvalidUnitException : Exception()
