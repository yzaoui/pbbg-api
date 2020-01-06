package com.bitwiserain.pbbg.domain.model.farm

import com.bitwiserain.pbbg.domain.model.BaseItem
import java.time.Duration

interface IBasePlant {
    val enum: PlantEnum
    val spriteName: String
    val growingPeriod: Duration

    interface Maturable : IBasePlant {
        val maturePeriod: Duration
    }
}

sealed class BasePlant : IBasePlant {
    object AppleTree : BasePlant(), IBasePlant.Maturable {
        override val enum = PlantEnum.APPLE_TREE
        override val spriteName = "apple-tree"
        override val growingPeriod: Duration = Duration.ofMinutes(1)
        override val maturePeriod: Duration = Duration.ofSeconds(40)
    }
    object TomatoPlant : BasePlant() {
        override val enum = PlantEnum.TOMATO_PLANT
        override val spriteName = "tomato-plant"
        override val growingPeriod: Duration = Duration.ofSeconds(20)
    }
}
