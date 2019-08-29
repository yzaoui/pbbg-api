package com.bitwiserain.pbbg.view.model

data class BaseItemJSON(
    val friendlyName: String,
    val imgURL: String,
    val description: String,
    val grid: Set<PointJSON>?
)
