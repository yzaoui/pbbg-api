package com.bitwiserain.pbbg.domain.model.battle

import kotlinx.serialization.Serializable

@Serializable
data class Turn(val unitId: Long, val counter: Int)
