package com.bitwiserain.pbbg.app.domain.model.mine

data class AvailableMines(
    val mines: List<MineType>,
    val nextUnlockLevel: Int?
)
