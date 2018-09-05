package com.bitwiserain.pbbg.view.model.mine

data class MineVM(
    val width: Int,
    val height: Int,
    val cells: List<List<MineItemVM?>>
)
