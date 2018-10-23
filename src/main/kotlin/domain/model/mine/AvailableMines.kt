package com.bitwiserain.pbbg.domain.model.mine

data class AvailableMines(
    val mines: List<MineType>,
    val nextUnlockLevel: Int?
)
