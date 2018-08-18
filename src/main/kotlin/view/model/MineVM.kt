package miner.view.model

data class MineVM(
    val width: Int,
    val height: Int,
    val content: List<List<MineItemVM?>>
)
