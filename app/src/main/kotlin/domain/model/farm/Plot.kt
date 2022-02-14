package com.bitwiserain.pbbg.domain.model.farm

data class Plot(
    val id: Long,
    val plant: Pair<Long, MaterializedPlant>?
)
