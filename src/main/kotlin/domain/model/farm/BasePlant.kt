package com.bitwiserain.pbbg.domain.model.farm

import java.time.Duration

interface IBasePlant {
    val enum: PlantEnum
    val friendlyName: String
    val description: String
    val spriteName: String
    val growingPeriod: Duration

    interface Maturable : IBasePlant {
        val maturePeriod: Duration
    }
}

sealed class BasePlant : IBasePlant {
    object AppleTree : BasePlant(), IBasePlant.Maturable {
        override val enum by lazy { PlantEnum.APPLE_TREE }
        override val friendlyName = "Apple Tree"
        override val description = "Apple Tree description here."
        override val spriteName = "apple-tree"
        override val growingPeriod: Duration = Duration.ofMinutes(1)
        override val maturePeriod: Duration = Duration.ofSeconds(40)
    }
    object TomatoPlant : BasePlant() {
        override val enum by lazy { PlantEnum.TOMATO_PLANT }
        override val friendlyName = "Tomato Plant"
        override val description = "Tomato Plant description here."
        override val spriteName = "tomato-plant"
        override val growingPeriod: Duration = Duration.ofSeconds(20)
    }
}
