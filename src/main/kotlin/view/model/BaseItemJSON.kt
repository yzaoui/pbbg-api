package com.bitwiserain.pbbg.view.model

data class BaseItemJSON(
    val friendlyName: String,
    val img16: String,
    val img32: String,
    val img64: String,
    val description: String,
    val grid: Set<PointJSON>?
)
