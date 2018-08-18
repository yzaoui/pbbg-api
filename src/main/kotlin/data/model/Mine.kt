package miner.data.model

data class Mine(val width: Int, val height: Int, val grid: Map<Pair<Int, Int>, MineItem>)
