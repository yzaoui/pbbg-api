package com.bitwiserain.pbbg.domain.model.mine

enum class MineType(val friendlyName: String, val minLevel: Int, private val mineEntityOdds: Map<Float, MineEntity>) {
    BEGINNER(
        "Young worm's mine",
        1,
        mapOf(
            0.05f to MineEntity.ROCK,
            0.01f to MineEntity.COAL
        )
    ),
    MODERATE(
        "Grown heron's mine",
        5,
        mapOf()
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
