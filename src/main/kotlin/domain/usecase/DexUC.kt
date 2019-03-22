package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.dex.DexItems
import com.bitwiserain.pbbg.domain.model.dex.DexUnits

interface DexUC {
    fun getDexItems(userId: Int): DexItems
    fun getDexUnits(userId: Int): DexUnits
}
