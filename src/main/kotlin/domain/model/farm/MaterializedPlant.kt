package com.bitwiserain.pbbg.domain.model.farm

import java.time.Clock
import java.time.Instant

interface IMaterializedPlant {
    val basePlant: IBasePlant
    val cycleStart: Instant
    fun canBeHarvested(now: Instant): Boolean = now >= cycleStart.plus(basePlant.growingPeriod)

    interface Maturable : IMaterializedPlant {
        override val basePlant: IBasePlant.Maturable
        val isFirstHarvest: Boolean
        override fun canBeHarvested(now: Instant): Boolean = if (isFirstHarvest) {
            super.canBeHarvested(now)
        } else {
            now >= cycleStart.plus(basePlant.maturePeriod)
        }

        // Plant is mature if it's been harvested before, or if its immature growing period is over
        fun isMature(now: Instant): Boolean = !isFirstHarvest || now >= cycleStart.plus(basePlant.growingPeriod)
    }
}

sealed class MaterializedPlant : IMaterializedPlant {
    data class AppleTree(override val cycleStart: Instant, override val isFirstHarvest: Boolean) : MaterializedPlant(), IMaterializedPlant.Maturable {
        override val basePlant get() = BasePlant.AppleTree
    }

    data class TomatoPlant(override val cycleStart: Instant) : MaterializedPlant() {
        override val basePlant get() = BasePlant.TomatoPlant
    }
}
