package com.bitwiserain.pbbg.domain.model.dex

import com.bitwiserain.pbbg.domain.model.MyUnitEnum

data class DexUnits(
    val discoveredUnits: Set<MyUnitEnum>,
    val lastUnitIsDiscovered: Boolean
)
