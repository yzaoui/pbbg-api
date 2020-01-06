package com.bitwiserain.pbbg.view.model.farm

data class BasePlantJSON(
    val growingPeriod: Long,
    val growingSprite: String,
    val maturePeriod: Long?,
    val matureSprite: String?
)
