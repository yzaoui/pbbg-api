package com.bitwiserain.pbbg.domain.model.mine

enum class MineType(val friendlyName: String, val minLevel: Int, private val mineEntityOdds: Map<Float, MineEntity>) {
    BEGINNER(
        "Young Worm's Mine",
        1,
        mapOf(
            0.05f to MineEntity.ROCK,
            0.01f to MineEntity.COAL
        )
    ),
    MODERATE(
        "Grown Heron's Mine",
        5,
        mapOf(
            0.048f to MineEntity.ROCK,
            0.002f to MineEntity.COAL,
            0.01f to MineEntity.COPPER
        )
    );

    fun rollForMineEntity(roll: Float): MineEntity? {
        var currentOdds = 0f

        for (pair in mineEntityOdds) {
            currentOdds += pair.key

            if (roll <= currentOdds) return pair.value
        }

        return null
    }
}
