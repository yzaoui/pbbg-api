package miner.view.model

data class MineVM(
    val width: Int,
    val height: Int,
    val cells: List<List<MineItemVM?>>
)
