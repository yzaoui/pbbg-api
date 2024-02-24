package com.bitwiserain.pbbg.app.domain.model.farm

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

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
        override val growingPeriod: Duration = 1.minutes
        override val maturePeriod: Duration = 40.seconds
    }
    data object TomatoPlant : BasePlant() {
        override val enum by lazy { PlantEnum.TOMATO_PLANT }
        override val friendlyName = "Tomato Plant"
        override val description = "Tomato Plant description here."
        override val spriteName = "tomato-plant"
        override val growingPeriod: Duration = 20.seconds
    }
}
