package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.dex.DexItems

interface DexUC {
    fun getDexItems(userId: Int): DexItems
}
