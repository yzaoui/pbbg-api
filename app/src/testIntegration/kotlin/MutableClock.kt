package com.bitwiserain.pbbg.app.testintegration

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalAmount

data class MutableClock(private var instant: Instant = Instant.now(), private val zone: ZoneId = ZoneId.systemDefault()) : Clock() {
    override fun getZone(): ZoneId = zone

    override fun withZone(zone: ZoneId): MutableClock = MutableClock(instant, zone)

    override fun instant(): Instant = instant

    operator fun plusAssign(amount: TemporalAmount) {
        instant = instant.plus(amount)
    }
}
