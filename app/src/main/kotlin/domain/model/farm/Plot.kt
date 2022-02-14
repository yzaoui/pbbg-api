package com.bitwiserain.pbbg.app.domain.model.farm

data class Plot(
    val id: Long,
    val plant: Pair<Long, MaterializedPlant>?
)
