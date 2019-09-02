package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.BaseItem
import com.bitwiserain.pbbg.domain.model.MyUnitEnum
import com.bitwiserain.pbbg.domain.model.dex.DexItems
import com.bitwiserain.pbbg.domain.model.dex.DexUnits

interface DexUC {
    fun getDexItems(userId: Int): DexItems
    /**
     * @throws InvalidItemException when [itemEnumId] is invalid.
     * @throws ItemUndiscoveredException when this item has not been discovered by this user.
     */
    fun getDexItem(userId: Int, itemEnumId: Int): BaseItem
    fun getDexUnits(userId: Int): DexUnits
    /**
     * @throws InvalidUnitException when [unitEnumId] is invalid.
     */
    fun getDexUnit(userId: Int, unitEnumId: Int): MyUnitEnum
}

class InvalidItemException : Exception()
class ItemUndiscoveredException : Exception()
class InvalidUnitException : Exception()
