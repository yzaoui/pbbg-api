package com.bitwiserain.pbbg.app.domain.model.dex

import com.bitwiserain.pbbg.app.domain.model.MyUnitEnum

data class DexUnits(
    val discoveredUnits: Set<MyUnitEnum>,
    val lastUnitId: Int
)
