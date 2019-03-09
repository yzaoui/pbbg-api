package com.bitwiserain.pbbg.domain.usecase

import com.bitwiserain.pbbg.domain.model.dex.Dex

interface DexUC {
    fun getDex(userId: Int): Dex
}
