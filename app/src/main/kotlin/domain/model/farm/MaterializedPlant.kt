package com.bitwiserain.pbbg.app.domain.model.farm

import java.time.Instant
import kotlin.time.toJavaDuration

interface IMaterializedPlant {
    val basePlant: IBasePlant
    val cycleStart: Instant

    fun canBeHarvested(now: Instant): Boolean = now >= cycleStart.plus(basePlant.growingPeriod.toJavaDuration())

    interface Maturable : IMaterializedPlant {
        override val basePlant: IBasePlant.Maturable
        val harvests: Int
        override fun canBeHarvested(now: Instant): Boolean = if (harvests == 0) {
            super.canBeHarvested(now)
        } else {
            now >= cycleStart.plus(basePlant.maturePeriod.toJavaDuration())
        }

        // Plant is mature if it's been harvested before, or if its immature growing period is over
        fun isMature(now: Instant): Boolean = harvests > 0 || now >= cycleStart.plus(basePlant.growingPeriod.toJavaDuration())
    }
}

sealed class MaterializedPlant : IMaterializedPlant {
    data class AppleTree(
        override val cycleStart: Instant,
        override val harvests: Int
    ) : MaterializedPlant(), IMaterializedPlant.Maturable {
        override val basePlant get() = BasePlant.AppleTree
    }

    data class TomatoPlant(
        override val cycleStart: Instant
    ) : MaterializedPlant() {
        override val basePlant get() = BasePlant.TomatoPlant
    }
}
